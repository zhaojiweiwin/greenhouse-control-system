package com.greenhouse.service;

import com.greenhouse.dto.TelemetryOverviewResponse;
import com.greenhouse.entity.SensorData;
import com.greenhouse.repository.SensorDataRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class TelemetryService {

  private final SensorDataRepository sensorDataRepository;
  private final DeviceService deviceService;
  private final RuleEngineService ruleEngineService;

  public TelemetryService(
      SensorDataRepository sensorDataRepository,
      DeviceService deviceService,
      RuleEngineService ruleEngineService) {
    this.sensorDataRepository = sensorDataRepository;
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

  public TelemetryOverviewResponse overview(String deviceId) {
    TelemetryOverviewResponse response = new TelemetryOverviewResponse();
    response.setLatestRows(
        latestRows().stream().map(this::toRow).collect(Collectors.toList())
    );
    response.setLatestSnapshot(buildSnapshot(deviceId));
    response.setTemperatureSeries(toSeries(deviceId, "temperature"));
    response.setHumiditySeries(toSeries(deviceId, "humidity"));
    response.setSoilMoistureSeries(toSeries(deviceId, "soil_moisture"));
    return response;
  }

  private Map<String, Object> buildSnapshot(String deviceId) {
    Map<String, Object> snapshot = new LinkedHashMap<>();
    snapshot.put("deviceId", deviceId);
    snapshot.put("temperature", latestMetricValue(deviceId, "temperature"));
    snapshot.put("humidity", latestMetricValue(deviceId, "humidity"));
    snapshot.put("soilMoisture", latestMetricValue(deviceId, "soil_moisture"));
    return snapshot;
  }

  private BigDecimal latestMetricValue(String deviceId, String metricType) {
    return sensorDataRepository.findTop20ByDeviceIdAndMetricTypeOrderByCollectTimeDesc(deviceId, metricType)
        .stream()
        .findFirst()
        .map(SensorData::getMetricValue)
        .orElse(BigDecimal.ZERO);
  }

  private List<Map<String, Object>> toSeries(String deviceId, String metricType) {
    return sensorDataRepository.findTop20ByDeviceIdAndMetricTypeOrderByCollectTimeDesc(deviceId, metricType)
        .stream()
        .map(item -> {
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
