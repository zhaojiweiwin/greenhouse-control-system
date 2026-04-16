package com.greenhouse.mqtt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenhouse.config.MqttProperties;
import com.greenhouse.service.TelemetryBroadcastService;
import java.util.Iterator;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MqttTelemetryListener {
  private static final Logger log = LoggerFactory.getLogger(MqttTelemetryListener.class);

  private final MqttClient mqttClient;
  private final MqttConnectOptions mqttConnectOptions;
  private final MqttProperties properties;
  private final ObjectMapper objectMapper;
  private final TelemetryBroadcastService telemetryBroadcastService;
  private final com.greenhouse.service.TelemetryService telemetryService;

  public MqttTelemetryListener(
      MqttClient mqttClient,
      MqttConnectOptions mqttConnectOptions,
      MqttProperties properties,
      ObjectMapper objectMapper,
      TelemetryBroadcastService telemetryBroadcastService,
      com.greenhouse.service.TelemetryService telemetryService) {
    this.mqttClient = mqttClient;
    this.mqttConnectOptions = mqttConnectOptions;
    this.properties = properties;
    this.objectMapper = objectMapper;
    this.telemetryBroadcastService = telemetryBroadcastService;
    this.telemetryService = telemetryService;
  }

  @PostConstruct
  public void subscribeTelemetry() {
    try {
      if (!mqttClient.isConnected()) {
        mqttClient.connect(mqttConnectOptions);
      }
      IMqttMessageListener listener = (topic, message) -> {
        JsonNode root = objectMapper.readTree(message.getPayload());
        String deviceId = root.path("deviceId").asText();
        JsonNode metrics = root.path("metrics");
        Iterator<Map.Entry<String, JsonNode>> fields = metrics.fields();
        telemetryService.saveTelemetry(deviceId, root.path("timestamp").asText(), fields);
        telemetryBroadcastService.broadcast(root.toString());
      };
      mqttClient.subscribe(properties.getTelemetryTopic(), 1, listener);
    } catch (Exception e) {
      log.warn("Skip MQTT subscribe because broker is unavailable: {}", e.getMessage());
    }
  }
}
