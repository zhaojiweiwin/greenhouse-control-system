package com.greenhouse.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "alarm_record")
public class AlarmRecord {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "device_id", nullable = false)
  private String deviceId;

  @Column(name = "alarm_type", nullable = false)
  private String alarmType;

  @Column(name = "alarm_level", nullable = false)
  private String alarmLevel;

  @Column(name = "trigger_value")
  private BigDecimal triggerValue;

  @Column(name = "threshold_value")
  private BigDecimal thresholdValue;

  @Column(nullable = false)
  private String status;

  @Column(nullable = false)
  private String message;

  @Column(name = "triggered_at", nullable = false)
  private LocalDateTime triggeredAt;

  @Column(name = "resolved_at")
  private LocalDateTime resolvedAt;

  public Long getId() { return id; }
  public String getDeviceId() { return deviceId; }
  public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
  public String getAlarmType() { return alarmType; }
  public void setAlarmType(String alarmType) { this.alarmType = alarmType; }
  public String getAlarmLevel() { return alarmLevel; }
  public void setAlarmLevel(String alarmLevel) { this.alarmLevel = alarmLevel; }
  public BigDecimal getTriggerValue() { return triggerValue; }
  public void setTriggerValue(BigDecimal triggerValue) { this.triggerValue = triggerValue; }
  public BigDecimal getThresholdValue() { return thresholdValue; }
  public void setThresholdValue(BigDecimal thresholdValue) { this.thresholdValue = thresholdValue; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public String getMessage() { return message; }
  public void setMessage(String message) { this.message = message; }
  public LocalDateTime getTriggeredAt() { return triggeredAt; }
  public void setTriggeredAt(LocalDateTime triggeredAt) { this.triggeredAt = triggeredAt; }
  public LocalDateTime getResolvedAt() { return resolvedAt; }
  public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}
