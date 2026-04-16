package com.greenhouse.service;

import com.greenhouse.dto.ControlCommandRequest;
import com.greenhouse.entity.ControlCommand;
import com.greenhouse.mqtt.MqttPublisher;
import com.greenhouse.repository.ControlCommandRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ControlService {

  private final ControlCommandRepository controlCommandRepository;
  private final MqttPublisher mqttPublisher;

  public ControlService(ControlCommandRepository controlCommandRepository, MqttPublisher mqttPublisher) {
    this.controlCommandRepository = controlCommandRepository;
    this.mqttPublisher = mqttPublisher;
  }

  public ControlCommand sendCommand(ControlCommandRequest request) {
    return saveAndPublish(request.getDeviceId(), request.getCommandType(), request.getPayload(), "MANUAL");
  }

  public ControlCommand sendAutomaticCommand(String deviceId, String commandType, String payload, String source) {
    return saveAndPublish(deviceId, commandType, payload, source);
  }

  public List<ControlCommand> latestCommands() {
    return controlCommandRepository.findTop20ByOrderBySentTimeDesc();
  }

  private ControlCommand saveAndPublish(String deviceId, String commandType, String payload, String source) {
    ControlCommand command = new ControlCommand();
    command.setDeviceId(deviceId);
    command.setCommandType(commandType);
    command.setCommandPayload(payload);
    command.setStatus("SENT");
    command.setSentTime(LocalDateTime.now());
    command.setSource(source);

    ControlCommand saved = controlCommandRepository.save(command);
    mqttPublisher.publishCommand(deviceId, payload);
    return saved;
  }
}
