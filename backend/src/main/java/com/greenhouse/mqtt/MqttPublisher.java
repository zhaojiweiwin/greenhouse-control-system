package com.greenhouse.mqtt;

import com.greenhouse.config.MqttProperties;
import java.nio.charset.StandardCharsets;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MqttPublisher {
  private static final Logger log = LoggerFactory.getLogger(MqttPublisher.class);

  private final MqttClient mqttClient;
  private final MqttConnectOptions mqttConnectOptions;
  private final MqttProperties properties;

  public MqttPublisher(MqttClient mqttClient, MqttConnectOptions mqttConnectOptions, MqttProperties properties) {
    this.mqttClient = mqttClient;
    this.mqttConnectOptions = mqttConnectOptions;
    this.properties = properties;
  }

  public void publishCommand(String deviceId, String payload) {
    try {
      if (!mqttClient.isConnected()) {
        mqttClient.connect(mqttConnectOptions);
      }
      String topic = properties.getCommandTopic().replace("{deviceId}", deviceId);
      MqttMessage message = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
      message.setQos(1);
      mqttClient.publish(topic, message);
    } catch (Exception e) {
      log.warn("Skip MQTT publish because broker is unavailable: {}", e.getMessage());
    }
  }
}
