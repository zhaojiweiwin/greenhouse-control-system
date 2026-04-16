package com.greenhouse.repository;

import com.greenhouse.entity.AlarmRecord;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AlarmRecordRepository extends JpaRepository<AlarmRecord, Long>, JpaSpecificationExecutor<AlarmRecord> {
  List<AlarmRecord> findTop20ByOrderByTriggeredAtDesc();
  Optional<AlarmRecord> findFirstByDeviceIdAndAlarmTypeAndStatusOrderByTriggeredAtDesc(String deviceId, String alarmType, String status);
}
