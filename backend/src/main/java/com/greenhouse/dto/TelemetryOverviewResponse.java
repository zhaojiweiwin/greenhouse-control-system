package com.greenhouse.dto;

import java.util.List;
import java.util.Map;

public class TelemetryOverviewResponse {

  private List<Map<String, Object>> latestRows;
  private Map<String, Object> latestSnapshot;
  private List<Map<String, Object>> temperatureSeries;
  private List<Map<String, Object>> humiditySeries;
  private List<Map<String, Object>> soilMoistureSeries;

  public List<Map<String, Object>> getLatestRows() { return latestRows; }
  public void setLatestRows(List<Map<String, Object>> latestRows) { this.latestRows = latestRows; }
  public Map<String, Object> getLatestSnapshot() { return latestSnapshot; }
  public void setLatestSnapshot(Map<String, Object> latestSnapshot) { this.latestSnapshot = latestSnapshot; }
  public List<Map<String, Object>> getTemperatureSeries() { return temperatureSeries; }
  public void setTemperatureSeries(List<Map<String, Object>> temperatureSeries) { this.temperatureSeries = temperatureSeries; }
  public List<Map<String, Object>> getHumiditySeries() { return humiditySeries; }
  public void setHumiditySeries(List<Map<String, Object>> humiditySeries) { this.humiditySeries = humiditySeries; }
  public List<Map<String, Object>> getSoilMoistureSeries() { return soilMoistureSeries; }
  public void setSoilMoistureSeries(List<Map<String, Object>> soilMoistureSeries) { this.soilMoistureSeries = soilMoistureSeries; }
}
