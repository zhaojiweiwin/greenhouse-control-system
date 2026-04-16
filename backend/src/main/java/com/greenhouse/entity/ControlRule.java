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
@Table(name = "control_rule")
public class ControlRule {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "rule_name", nullable = false)
  private String ruleName;

  @Column(name = "device_id", nullable = false)
  private String deviceId;

  @Column(name = "metric_type", nullable = false)
  private String metricType;

  @Column(nullable = false)
  private String operator;

  @Column(name = "threshold_value", nullable = false)
  private BigDecimal thresholdValue;

  @Column(name = "action_type", nullable = false)
  private String actionType;

  @Column(name = "action_payload", nullable = false, columnDefinition = "json")
  private String actionPayload;

  @Column(nullable = false)
  private Boolean enabled;

  @Column(name = "cooldown_seconds", nullable = false)
  private Integer cooldownSeconds;

  @Column(name = "last_triggered_at")
  private LocalDateTime lastTriggeredAt;

  public Long getId() { return id; }
  public String getRuleName() { return ruleName; }
  public void setRuleName(String ruleName) { this.ruleName = ruleName; }
  public String getDeviceId() { return deviceId; }
  public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
  public String getMetricType() { return metricType; }
  public void setMetricType(String metricType) { this.metricType = metricType; }
  public String getOperator() { return operator; }
  public void setOperator(String operator) { this.operator = operator; }
  public BigDecimal getThresholdValue() { return thresholdValue; }
  public void setThresholdValue(BigDecimal thresholdValue) { this.thresholdValue = thresholdValue; }
  public String getActionType() { return actionType; }
  public void setActionType(String actionType) { this.actionType = actionType; }
  public String getActionPayload() { return actionPayload; }
  public void setActionPayload(String actionPayload) { this.actionPayload = actionPayload; }
  public Boolean getEnabled() { return enabled; }
  public void setEnabled(Boolean enabled) { this.enabled = enabled; }
  public Integer getCooldownSeconds() { return cooldownSeconds; }
  public void setCooldownSeconds(Integer cooldownSeconds) { this.cooldownSeconds = cooldownSeconds; }
  public LocalDateTime getLastTriggeredAt() { return lastTriggeredAt; }
  public void setLastTriggeredAt(LocalDateTime lastTriggeredAt) { this.lastTriggeredAt = lastTriggeredAt; }
}
