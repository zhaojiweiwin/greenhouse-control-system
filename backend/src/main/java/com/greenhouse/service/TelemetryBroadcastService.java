package com.greenhouse.service;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Service
public class TelemetryBroadcastService {

  private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

  public void register(WebSocketSession session) {
    sessions.add(session);
  }

  public void unregister(WebSocketSession session) {
    sessions.remove(session);
  }

  public void broadcast(String message) {
    sessions.removeIf(session -> {
      try {
        if (!session.isOpen()) {
          return true;
        }
        session.sendMessage(new TextMessage(message));
        return false;
      } catch (IOException e) {
        return true;
      }
    });
  }
}
