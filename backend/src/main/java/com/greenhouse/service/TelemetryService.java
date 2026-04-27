package com.greenhouse.service;

import com.greenhouse.dto.TelemetryOverviewResponse;
import com.greenhouse.entity.Device;
import com.greenhouse.entity.SensorData;
import com.greenhouse.repository.DeviceRepository;
import com.greenhouse.repository.SensorDataRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TelemetryService {

  private static final int HISTORY_MAX_POINTS = 2000;
  private static final Set<String> HISTORY_METRICS =
      Collections.unmodifiableSet(
          new HashSet<>(
              Arrays.asList("temperature", "humidity", "illuminance", "soil_moisture")));

  private final SensorDataRepository sensorDataRepository;
  private final DeviceRepository deviceRepository;
  private final DeviceService deviceService;
  private final RuleEngineService ruleEngineService;

  public TelemetryService(
      SensorDataRepository sensorDataRepository,
      DeviceRepository deviceRepository,
      DeviceService deviceService,
      RuleEngineService ruleEngineService) {
    this.sensorDataRepository = sensorDataRepository;
    this.deviceRepository = deviceRepository;
    this.deviceService = deviceService;
    this.ruleEngineService = ruleEngineService;
  }

  public void saveTelemetry(String deviceId, String timestamp, Iterator<Map.Entry<String, com.fasterxml.jackson.databind.JsonNode>> fields) {
    LocalDateTime collectTime = OffsetDateTime.parse(timestamp).toLocalDateTime();
    Map<String, BigDecimal> metrics = new HashMap<>();
    while (fields.hasNext()) {
      Map.Entry<String, com.fasterxml.jackson.databind.JsonNode> metric = fields.next();
      BigDecimal value = BigDecimal.valueOf(metric.getValue().asDouble());
      SensorData data = new SensorData();
      data.setDeviceId(deviceId);
      data.setMetricType(metric.getKey());
      data.setMetricValue(value);
      data.setCollectTime(collectTime);
      sensorDataRepository.save(data);
      metrics.put(metric.getKey(), value);
    }
    deviceService.touchOnline(deviceId, collectTime);
    ruleEngineService.evaluate(deviceId, metrics);
  }

  public List<SensorData> latestRows() {
    return sensorDataRepository.findTop50ByOrderByCollectTimeDesc();
  }

  /**
   * 按「锚点设备」所在实验温室，查询指定指标在时间范围内的历史点（时间升序，最多
   * {@value #HISTORY_MAX_POINTS} 条）。
   */
  public List<Map<String, Object>> historySeries(String anchorDeviceId, String metricType, int hours) {
    if (metricType == null || !HISTORY_METRICS.contains(metricType.trim())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "metricType 须为 temperature、humidity、illuminance、soil_moisture 之一");
    }
    String type = metricType.trim();
    int h = Math.min(Math.max(hours, 1), 24 * 60);
    List<String> ids = greenhouseDeviceIds(anchorDeviceId);
    LocalDateTime end = LocalDateTime.now();
    LocalDateTime start = end.minusHours(h);
    List<SensorData> rows =
        sensorDataRepository.findHistoryByDevicesAndMetricBetween(
            ids, type, start, end, PageRequest.of(0, HISTORY_MAX_POINTS));
    return rows.stream()
        .map(
            s -> {
              Map<String, Object> row = new LinkedHashMap<>();
              row.put("collectTime", s.getCollectTime());
              row.put("value", s.getMetricValue());
              return row;
            })
        .collect(Collectors.toList());
  }

  public TelemetryOverviewResponse overview(String deviceId) {
    TelemetryOverviewResponse response = new TelemetryOverviewResponse();
    response.setLatestRows(
        latestRows().stream().map(this::toRow).collect(Collectors.toList())
    );
    List<String> greenhouseIds = greenhouseDeviceIds(deviceId);
    response.setLatestSnapshot(buildSnapshot(deviceId, greenhouseIds));
    response.setTemperatureSeries(toSeriesAmong(greenhouseIds, "temperature"));
    response.setHumiditySeries(toSeriesAmong(greenhouseIds, "humidity"));
    response.setIlluminanceSeries(toSeriesAmong(greenhouseIds, "illuminance"));
    response.setSoilMoistureSeries(toSeriesAmong(greenhouseIds, "soil_moisture"));
    return response;
  }

  /** 与当前设备同一温室下的全部设备 ID，用于聚合展示环境指标趋势 */
  private List<String> greenhouseDeviceIds(String anchorDeviceId) {
    return deviceRepository
        .findByDeviceId(anchorDeviceId)
        .map(Device::getGreenhouseName)
        .map(deviceRepository::findByGreenhouseNameOrderByDeviceIdAsc)
        .map(
            list ->
                list.stream().map(Device::getDeviceId).collect(Collectors.toList()))
        .filter(list -> !list.isEmpty())
        .orElse(Collections.singletonList(anchorDeviceId));
  }

  private Map<String, Object> buildSnapshot(String deviceId, List<String> greenhouseDeviceIds) {
    Map<String, Object> snapshot = new LinkedHashMap<>();
    snapshot.put("deviceId", deviceId);
    snapshot.put("temperature", latestMetricAmong(greenhouseDeviceIds, "temperature"));
    snapshot.put("humidity", latestMetricAmong(greenhouseDeviceIds, "humidity"));
    snapshot.put("soilMoisture", latestMetricAmong(greenhouseDeviceIds, "soil_moisture"));
    snapshot.put("illuminance", latestMetricAmong(greenhouseDeviceIds, "illuminance"));
    return snapshot;
  }

  private BigDecimal latestMetricAmong(List<String> deviceIds, String metricType) {
    if (deviceIds == null || deviceIds.isEmpty()) {
      return BigDecimal.ZERO;
    }
    return sensorDataRepository
        .findByDeviceIdInAndMetricTypeOrderByCollectTimeDesc(
            deviceIds, metricType, PageRequest.of(0, 1))
        .stream()
        .findFirst()
        .map(SensorData::getMetricValue)
        .orElse(BigDecimal.ZERO);
  }

  private List<Map<String, Object>> toSeriesAmong(List<String> deviceIds, String metricType) {
    if (deviceIds == null || deviceIds.isEmpty()) {
      return Collections.emptyList();
    }
    return sensorDataRepository
        .findByDeviceIdInAndMetricTypeOrderByCollectTimeDesc(
            deviceIds, metricType, PageRequest.of(0, 10))
        .stream()
        .map(
            item -> {
              Map<String, Object> row = new LinkedHashMap<>();
              row.put("time", item.getCollectTime());
              row.put("value", item.getMetricValue());
              return row;
            })
        .collect(Collectors.toList());
  }

  private Map<String, Object> toRow(SensorData item) {
    Map<String, Object> row = new LinkedHashMap<>();
    row.put("id", item.getId());
    row.put("deviceId", item.getDeviceId());
    row.put("metricType", item.getMetricType());
    row.put("metricValue", item.getMetricValue());
    row.put("collectTime", item.getCollectTime());
    return row;
  }
}
