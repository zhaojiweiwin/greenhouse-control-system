package com.greenhouse.controller;

import com.greenhouse.dto.DeviceRequest;
import com.greenhouse.entity.Device;
import com.greenhouse.service.DeviceService;
import javax.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

  private final DeviceService deviceService;

  public DeviceController(DeviceService deviceService) {
    this.deviceService = deviceService;
  }

  @GetMapping
  public List<Device> list() {
    return deviceService.findAll();
  }

  @PostMapping
  public Device save(@Valid @RequestBody DeviceRequest request) {
    return deviceService.save(request);
  }
}
