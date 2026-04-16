package com.greenhouse.controller;

import com.greenhouse.dto.AlarmQueryRequest;
import com.greenhouse.entity.AlarmRecord;
import com.greenhouse.service.AlarmService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/alarms")
public class AlarmController {

  private final AlarmService alarmService;

  public AlarmController(AlarmService alarmService) {
    this.alarmService = alarmService;
  }

  @GetMapping
  public List<AlarmRecord> latest(@ModelAttribute AlarmQueryRequest request) {
    if (request.getDeviceId() == null
        && request.getAlarmType() == null
        && request.getStatus() == null
        && request.getStartTime() == null
        && request.getEndTime() == null) {
      return alarmService.latest();
    }
    return alarmService.query(request);
  }
}
