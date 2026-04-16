package com.greenhouse.repository;

import com.greenhouse.entity.SensorData;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorDataRepository extends JpaRepository<SensorData, Long> {
  List<SensorData> findTop50ByOrderByCollectTimeDesc();
  List<SensorData> findTop20ByDeviceIdAndMetricTypeOrderByCollectTimeDesc(String deviceId, String metricType);
  List<SensorData> findByCollectTimeAfterOrderByCollectTimeDesc(LocalDateTime collectTime);
}
