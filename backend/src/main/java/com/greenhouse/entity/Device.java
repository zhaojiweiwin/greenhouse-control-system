package com.greenhouse.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "device")
public class Device {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "device_id", nullable = false, unique = true)
  private String deviceId;

  @Column(nullable = false)
  private String name;

  @Column(name = "greenhouse_name", nullable = false)
  private String greenhouseName;

  @Column(name = "control_mode", nullable = false)
  private String controlMode;

  @Column(name = "online_status", nullable = false)
  private Boolean onlineStatus;

  @Column(name = "last_heartbeat")
  private LocalDateTime lastHeartbeat;

  public Long getId() { return id; }
  public String getDeviceId() { return deviceId; }
  public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public String getGreenhouseName() { return greenhouseName; }
  public void setGreenhouseName(String greenhouseName) { this.greenhouseName = greenhouseName; }
  public String getControlMode() { return controlMode; }
  public void setControlMode(String controlMode) { this.controlMode = controlMode; }
  public Boolean getOnlineStatus() { return onlineStatus; }
  public void setOnlineStatus(Boolean onlineStatus) { this.onlineStatus = onlineStatus; }
  public LocalDateTime getLastHeartbeat() { return lastHeartbeat; }
  public void setLastHeartbeat(LocalDateTime lastHeartbeat) { this.lastHeartbeat = lastHeartbeat; }
}
