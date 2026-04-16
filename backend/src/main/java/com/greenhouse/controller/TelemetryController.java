package com.greenhouse.controller;

import com.greenhouse.dto.TelemetryOverviewResponse;
import com.greenhouse.entity.SensorData;
import com.greenhouse.service.TelemetryService;
import java.util.List;
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
}
