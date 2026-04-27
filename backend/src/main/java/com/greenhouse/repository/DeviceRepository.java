package com.greenhouse.repository;

import com.greenhouse.entity.Device;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRepository extends JpaRepository<Device, Long> {
  Optional<Device> findByDeviceId(String deviceId);

  List<Device> findByGreenhouseNameOrderByDeviceIdAsc(String greenhouseName);
}
