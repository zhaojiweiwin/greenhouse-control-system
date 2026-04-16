package com.greenhouse.dto;

import javax.validation.constraints.NotBlank;

public class DeviceRequest {

  @NotBlank
  private String deviceId;

  @NotBlank
  private String name;

  @NotBlank
  private String greenhouseName;

  private String controlMode = "AUTO";

  public String getDeviceId() { return deviceId; }
  public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public String getGreenhouseName() { return greenhouseName; }
  public void setGreenhouseName(String greenhouseName) { this.greenhouseName = greenhouseName; }
  public String getControlMode() { return controlMode; }
  public void setControlMode(String controlMode) { this.controlMode = controlMode; }
}
