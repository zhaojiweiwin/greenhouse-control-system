package com.greenhouse.service;

import com.greenhouse.dto.AlarmQueryRequest;
import com.greenhouse.entity.AlarmRecord;
import com.greenhouse.repository.AlarmRecordRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import javax.persistence.criteria.Predicate;
import org.springframework.stereotype.Service;

@Service
public class AlarmService {

  private final AlarmRecordRepository alarmRecordRepository;

  public AlarmService(AlarmRecordRepository alarmRecordRepository) {
    this.alarmRecordRepository = alarmRecordRepository;
  }

  public List<AlarmRecord> latest() {
    return alarmRecordRepository.findTop20ByOrderByTriggeredAtDesc();
  }

  public List<AlarmRecord> query(AlarmQueryRequest request) {
    return alarmRecordRepository.findAll((root, query, cb) -> {
      List<Predicate> predicates = new java.util.ArrayList<Predicate>();
      if (request.getDeviceId() != null && !request.getDeviceId().trim().isEmpty()) {
        predicates.add(cb.equal(root.get("deviceId"), request.getDeviceId().trim()));
      }
      if (request.getAlarmType() != null && !request.getAlarmType().trim().isEmpty()) {
        predicates.add(cb.equal(root.get("alarmType"), request.getAlarmType().trim()));
      }
      if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
        predicates.add(cb.equal(root.get("status"), request.getStatus().trim()));
      }
      if (request.getStartTime() != null) {
        predicates.add(cb.greaterThanOrEqualTo(root.get("triggeredAt"), request.getStartTime()));
      }
      if (request.getEndTime() != null) {
        predicates.add(cb.lessThanOrEqualTo(root.get("triggeredAt"), request.getEndTime()));
      }
      query.orderBy(cb.desc(root.get("triggeredAt")));
      return cb.and(predicates.toArray(new Predicate[0]));
    });
  }

  public void raiseIfAbsent(String deviceId, String alarmType, BigDecimal triggerValue, BigDecimal thresholdValue, String message) {
    Optional<AlarmRecord> existing = alarmRecordRepository
        .findFirstByDeviceIdAndAlarmTypeAndStatusOrderByTriggeredAtDesc(deviceId, alarmType, "OPEN");
    if (existing.isPresent()) {
      return;
    }
    AlarmRecord alarm = new AlarmRecord();
    alarm.setDeviceId(deviceId);
    alarm.setAlarmType(alarmType);
    alarm.setAlarmLevel("WARN");
    alarm.setTriggerValue(triggerValue);
    alarm.setThresholdValue(thresholdValue);
    alarm.setStatus("OPEN");
    alarm.setMessage(message);
    alarm.setTriggeredAt(LocalDateTime.now());
    alarmRecordRepository.save(alarm);
  }

  public void resolve(String deviceId, String alarmType) {
    alarmRecordRepository
        .findFirstByDeviceIdAndAlarmTypeAndStatusOrderByTriggeredAtDesc(deviceId, alarmType, "OPEN")
        .ifPresent(alarm -> {
          alarm.setStatus("RESOLVED");
          alarm.setResolvedAt(LocalDateTime.now());
          alarmRecordRepository.save(alarm);
        });
  }
}
