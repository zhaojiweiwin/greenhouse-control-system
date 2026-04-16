package com.greenhouse.config;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MqttProperties.class)
public class MqttConfig {
  private static final Logger log = LoggerFactory.getLogger(MqttConfig.class);

  @Bean
  public MqttConnectOptions mqttConnectOptions(MqttProperties properties) {
    MqttConnectOptions options = new MqttConnectOptions();
    options.setAutomaticReconnect(true);
    options.setCleanSession(false);
    if (properties.getUsername() != null && !properties.getUsername().trim().isEmpty()) {
      options.setUserName(properties.getUsername());
    }
    if (properties.getPassword() != null && !properties.getPassword().trim().isEmpty()) {
      options.setPassword(properties.getPassword().toCharArray());
    }
    return options;
  }

  @Bean
  public MqttClient mqttClient(MqttProperties properties, MqttConnectOptions options) throws Exception {
    MqttClient client = new MqttClient(
        properties.getBrokerUrl(),
        properties.getClientId(),
        new MemoryPersistence());
    try {
      client.connect(options);
    } catch (Exception e) {
      log.warn("MQTT broker unavailable, backend starts without MQTT: {}", e.getMessage());
    }
    return client;
  }
}
