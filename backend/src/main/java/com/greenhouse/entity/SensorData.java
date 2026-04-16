package com.greenhouse.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sensor_data")
public class SensorData {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "device_id", nullable = false)
  private String deviceId;

  @Column(name = "metric_type", nullable = false)
  private String metricType;

  @Column(name = "metric_value", nullable = false)
  private BigDecimal metricValue;

  @Column(name = "collect_time", nullable = false)
  private LocalDateTime collectTime;

  public Long getId() { return id; }
  public String getDeviceId() { return deviceId; }
  public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
  public String getMetricType() { return metricType; }
  public void setMetricType(String metricType) { this.metricType = metricType; }
  public BigDecimal getMetricValue() { return metricValue; }
  public void setMetricValue(BigDecimal metricValue) { this.metricValue = metricValue; }
  public LocalDateTime getCollectTime() { return collectTime; }
  public void setCollectTime(LocalDateTime collectTime) { this.collectTime = collectTime; }
}
