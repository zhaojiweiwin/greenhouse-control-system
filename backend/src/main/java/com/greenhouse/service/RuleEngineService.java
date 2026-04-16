package com.greenhouse.service;

import com.greenhouse.entity.ControlRule;
import com.greenhouse.repository.ControlRuleRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class RuleEngineService {

  private final ControlRuleRepository controlRuleRepository;
  private final ControlService controlService;
  private final AlarmService alarmService;

  public RuleEngineService(
      ControlRuleRepository controlRuleRepository,
      ControlService controlService,
      AlarmService alarmService) {
    this.controlRuleRepository = controlRuleRepository;
    this.controlService = controlService;
    this.alarmService = alarmService;
  }

  public List<ControlRule> findAll() {
    return controlRuleRepository.findAll();
  }

  public ControlRule save(ControlRule rule) {
    return controlRuleRepository.save(rule);
  }

  public void evaluate(String deviceId, Map<String, BigDecimal> metrics) {
    for (ControlRule rule : controlRuleRepository.findByEnabledTrueOrderByIdAsc()) {
      if (!rule.getDeviceId().equals(deviceId)) {
        continue;
      }
      BigDecimal currentValue = metrics.get(rule.getMetricType());
      if (currentValue == null) {
        continue;
      }
      if (!matches(rule, currentValue)) {
        alarmService.resolve(deviceId, "RULE_" + rule.getId());
        continue;
      }
      if (isCoolingDown(rule)) {
        continue;
      }
      controlService.sendAutomaticCommand(deviceId, rule.getActionType(), rule.getActionPayload(), "RULE:" + rule.getRuleName());
      rule.setLastTriggeredAt(LocalDateTime.now());
      controlRuleRepository.save(rule);
      alarmService.raiseIfAbsent(
          deviceId,
          "RULE_" + rule.getId(),
          currentValue,
          rule.getThresholdValue(),
          "规则触发: " + rule.getRuleName());
    }
  }

  private boolean matches(ControlRule rule, BigDecimal currentValue) {
    String operator = rule.getOperator();
    if (">".equals(operator)) {
      return currentValue.compareTo(rule.getThresholdValue()) > 0;
    }
    if (">=".equals(operator)) {
      return currentValue.compareTo(rule.getThresholdValue()) >= 0;
    }
    if ("<".equals(operator)) {
      return currentValue.compareTo(rule.getThresholdValue()) < 0;
    }
    if ("<=".equals(operator)) {
      return currentValue.compareTo(rule.getThresholdValue()) <= 0;
    }
    return false;
  }

  private boolean isCoolingDown(ControlRule rule) {
    if (rule.getLastTriggeredAt() == null) {
      return false;
    }
    return rule.getLastTriggeredAt().plusSeconds(rule.getCooldownSeconds()).isAfter(LocalDateTime.now());
  }
}
