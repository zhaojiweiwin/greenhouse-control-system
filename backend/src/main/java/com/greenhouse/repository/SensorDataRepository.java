package com.greenhouse.repository;

import com.greenhouse.entity.SensorData;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SensorDataRepository extends JpaRepository<SensorData, Long> {
  List<SensorData> findTop50ByOrderByCollectTimeDesc();
  List<SensorData> findTop20ByDeviceIdAndMetricTypeOrderByCollectTimeDesc(String deviceId, String metricType);

  /**
   * 同一温室内多设备按指标聚合取最近记录。显式 JPQL，避免派生方法名在 In + Top + OrderBy
   * 组合下生成错误 SQL 导致光照度、土壤湿度等始终查不到。
   */
  @Query(
      "SELECT s FROM SensorData s WHERE s.deviceId IN :deviceIds AND s.metricType = :metricType "
          + "ORDER BY s.collectTime DESC")
  List<SensorData> findByDeviceIdInAndMetricTypeOrderByCollectTimeDesc(
      @Param("deviceIds") Collection<String> deviceIds,
      @Param("metricType") String metricType,
      Pageable pageable);

  List<SensorData> findByCollectTimeAfterOrderByCollectTimeDesc(LocalDateTime collectTime);

  @Query(
      "SELECT s FROM SensorData s WHERE s.deviceId IN :deviceIds AND s.metricType = :metricType "
          + "AND s.collectTime >= :start AND s.collectTime <= :end ORDER BY s.collectTime ASC")
  List<SensorData> findHistoryByDevicesAndMetricBetween(
      @Param("deviceIds") Collection<String> deviceIds,
      @Param("metricType") String metricType,
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end,
      Pageable pageable);
}
