package com.greenhouse.dto;

import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

public class AlarmQueryRequest {

  private String deviceId;
  private String alarmType;
  private String status;

  @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
  private LocalDateTime startTime;

  @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
  private LocalDateTime endTime;

  public String getDeviceId() { return deviceId; }
  public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
  public String getAlarmType() { return alarmType; }
  public void setAlarmType(String alarmType) { this.alarmType = alarmType; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public LocalDateTime getStartTime() { return startTime; }
  public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
  public LocalDateTime getEndTime() { return endTime; }
  public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
}
