package com.greenhouse.controller;

import com.greenhouse.dto.TelemetryOverviewResponse;
import com.greenhouse.entity.SensorData;
import com.greenhouse.service.TelemetryService;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/telemetry")
public class TelemetryController {

  private final TelemetryService telemetryService;

  public TelemetryController(TelemetryService telemetryService) {
    this.telemetryService = telemetryService;
  }

  @GetMapping("/latest")
  public List<SensorData> latest() {
    return telemetryService.latestRows();
  }

  @GetMapping("/overview")
  public TelemetryOverviewResponse overview(@RequestParam(defaultValue = "gh-esp32-01") String deviceId) {
    return telemetryService.overview(deviceId);
  }

  /**
   * 历史趋势：锚点设备用于解析所在温室，指标为四选一，hours 为回溯小时数（上限约 60 天）。
   */
  @GetMapping("/history")
  public List<Map<String, Object>> history(
      @RequestParam String metricType,
      @RequestParam(defaultValue = "gh-esp32-01") String deviceId,
      @RequestParam(defaultValue = "168") int hours) {
    return telemetryService.historySeries(deviceId, metricType, hours);
  }
}
