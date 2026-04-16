package com.greenhouse.controller;

import com.greenhouse.dto.ControlRuleRequest;
import com.greenhouse.entity.ControlRule;
import com.greenhouse.service.RuleEngineService;
import javax.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/strategies")
public class StrategyController {

  private final RuleEngineService ruleEngineService;

  public StrategyController(RuleEngineService ruleEngineService) {
    this.ruleEngineService = ruleEngineService;
  }

  @GetMapping
  public List<ControlRule> list() {
    return ruleEngineService.findAll();
  }

  @PostMapping
  public ControlRule save(@Valid @RequestBody ControlRuleRequest request) {
    ControlRule rule = new ControlRule();
    rule.setRuleName(request.getRuleName());
    rule.setDeviceId(request.getDeviceId());
    rule.setMetricType(request.getMetricType());
    rule.setOperator(request.getOperator());
    rule.setThresholdValue(request.getThresholdValue());
    rule.setActionType(request.getActionType());
    rule.setActionPayload(request.getActionPayload());
    rule.setEnabled(request.getEnabled());
    rule.setCooldownSeconds(request.getCooldownSeconds());
    return ruleEngineService.save(rule);
  }
}
