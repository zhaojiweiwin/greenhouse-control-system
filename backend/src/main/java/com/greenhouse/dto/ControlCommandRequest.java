package com.greenhouse.dto;

import javax.validation.constraints.NotBlank;

public class ControlCommandRequest {

  @NotBlank
  private String deviceId;
  @NotBlank
  private String commandType;
  @NotBlank
  private String payload;

  public String getDeviceId() { return deviceId; }
  public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
  public String getCommandType() { return commandType; }
  public void setCommandType(String commandType) { this.commandType = commandType; }
  public String getPayload() { return payload; }
  public void setPayload(String payload) { this.payload = payload; }
}
