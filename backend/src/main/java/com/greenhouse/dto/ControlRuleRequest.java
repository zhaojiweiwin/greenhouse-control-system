package com.greenhouse.dto;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public class ControlRuleRequest {

  @NotBlank
  private String ruleName;

  @NotBlank
  private String deviceId;

  @NotBlank
  private String metricType;

  @NotBlank
  private String operator;

  @NotNull
  @DecimalMin("0.0")
  private BigDecimal thresholdValue;

  @NotBlank
  private String actionType;

  @NotBlank
  private String actionPayload;

  private Boolean enabled = true;

  @PositiveOrZero
  private Integer cooldownSeconds = 60;

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
}
