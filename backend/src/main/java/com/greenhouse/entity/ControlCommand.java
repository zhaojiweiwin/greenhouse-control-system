package com.greenhouse.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "control_command")
public class ControlCommand {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "device_id", nullable = false)
  private String deviceId;

  @Column(name = "command_type", nullable = false)
  private String commandType;

  @Column(name = "command_payload", nullable = false, columnDefinition = "json")
  private String commandPayload;

  @Column(name = "status", nullable = false)
  private String status;

  @Column(name = "sent_time", nullable = false)
  private LocalDateTime sentTime;

  @Column(name = "ack_time")
  private LocalDateTime ackTime;

  @Column(nullable = false)
  private String source;

  public Long getId() { return id; }
  public String getDeviceId() { return deviceId; }
  public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
  public String getCommandType() { return commandType; }
  public void setCommandType(String commandType) { this.commandType = commandType; }
  public String getCommandPayload() { return commandPayload; }
  public void setCommandPayload(String commandPayload) { this.commandPayload = commandPayload; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public LocalDateTime getSentTime() { return sentTime; }
  public void setSentTime(LocalDateTime sentTime) { this.sentTime = sentTime; }
  public LocalDateTime getAckTime() { return ackTime; }
  public void setAckTime(LocalDateTime ackTime) { this.ackTime = ackTime; }
  public String getSource() { return source; }
  public void setSource(String source) { this.source = source; }
}
