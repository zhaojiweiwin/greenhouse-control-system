package com.greenhouse.service;

import com.greenhouse.dto.ControlRuleRequest;
import com.greenhouse.entity.ControlRule;
import com.greenhouse.repository.ControlRuleRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

  public ControlRule updateRule(Long id, ControlRuleRequest request) {
    ControlRule rule = controlRuleRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rule not found: " + id));
    rule.setRuleName(request.getRuleName());
    rule.setDeviceId(request.getDeviceId());
    rule.setMetricType(request.getMetricType());
    rule.setOperator(request.getOperator());
    rule.setThresholdValue(request.getThresholdValue());
    rule.setActionType(request.getActionType());
    rule.setActionPayload(request.getActionPayload());
    rule.setEnabled(request.getEnabled());
    rule.setCooldownSeconds(request.getCooldownSeconds());
    return controlRuleRepository.save(rule);
  }

  /**
   * 删除控制规则。方法名刻意不用 {@code deleteById}，避免部分 IDE/语言服务与
   * {@link org.springframework.data.repository.CrudRepository#deleteById} 混淆产生“重复方法”误报。
   */
  public void deleteRule(Long id) {
    if (!controlRuleRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Rule not found: " + id);
    }
    controlRuleRepository.deleteById(id);
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
