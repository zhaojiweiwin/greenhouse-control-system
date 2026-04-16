package com.greenhouse.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mqtt")
public class MqttProperties {

  private String brokerUrl;
  private String clientId;
  private String username;
  private String password;
  private String telemetryTopic;
  private String commandTopic;

  public String getBrokerUrl() { return brokerUrl; }
  public void setBrokerUrl(String brokerUrl) { this.brokerUrl = brokerUrl; }
  public String getClientId() { return clientId; }
  public void setClientId(String clientId) { this.clientId = clientId; }
  public String getUsername() { return username; }
  public void setUsername(String username) { this.username = username; }
  public String getPassword() { return password; }
  public void setPassword(String password) { this.password = password; }
  public String getTelemetryTopic() { return telemetryTopic; }
  public void setTelemetryTopic(String telemetryTopic) { this.telemetryTopic = telemetryTopic; }
  public String getCommandTopic() { return commandTopic; }
  public void setCommandTopic(String commandTopic) { this.commandTopic = commandTopic; }
}
