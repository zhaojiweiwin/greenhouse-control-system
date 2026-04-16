package com.greenhouse.service;

import com.greenhouse.dto.DeviceRequest;
import com.greenhouse.entity.Device;
import com.greenhouse.repository.DeviceRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DeviceService {

  private final DeviceRepository deviceRepository;

  public DeviceService(DeviceRepository deviceRepository) {
    this.deviceRepository = deviceRepository;
  }

  public List<Device> findAll() {
    return deviceRepository.findAll();
  }

  public Device save(DeviceRequest request) {
    Device device = deviceRepository.findByDeviceId(request.getDeviceId()).orElseGet(Device::new);
    device.setDeviceId(request.getDeviceId());
    device.setName(request.getName());
    device.setGreenhouseName(request.getGreenhouseName());
    device.setControlMode(request.getControlMode());
    if (device.getOnlineStatus() == null) {
      device.setOnlineStatus(false);
    }
    return deviceRepository.save(device);
  }

  public void touchOnline(String deviceId, LocalDateTime heartbeatTime) {
    Device device = deviceRepository.findByDeviceId(deviceId).orElseGet(Device::new);
    if (device.getDeviceId() == null) {
      device.setDeviceId(deviceId);
      device.setName(deviceId);
      device.setGreenhouseName("默认温室");
      device.setControlMode("AUTO");
    }
    device.setOnlineStatus(true);
    device.setLastHeartbeat(heartbeatTime);
    deviceRepository.save(device);
  }
}
