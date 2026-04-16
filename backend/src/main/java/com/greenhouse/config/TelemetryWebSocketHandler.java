package com.greenhouse.config;

import com.greenhouse.service.TelemetryBroadcastService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class TelemetryWebSocketHandler extends TextWebSocketHandler {

  private final TelemetryBroadcastService telemetryBroadcastService;

  public TelemetryWebSocketHandler(TelemetryBroadcastService telemetryBroadcastService) {
    this.telemetryBroadcastService = telemetryBroadcastService;
  }

  @Override
  public void afterConnectionEstablished(WebSocketSession session) {
    telemetryBroadcastService.register(session);
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    telemetryBroadcastService.unregister(session);
  }
}
