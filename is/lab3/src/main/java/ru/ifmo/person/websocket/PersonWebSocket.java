package ru.ifmo.person.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@ServerEndpoint("/ws/persons")
public class PersonWebSocket {

    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        System.out.println("WebSocket opened: " + session.getId());
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        System.out.println("WebSocket closed: " + session.getId());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("WebSocket error: " + throwable.getMessage());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("Received message: " + message);
    }

    public static void notifyClients(String action, Long personId) {
        synchronized (sessions) {
            for (Session session : sessions) {
                if (session.isOpen()) {
                    try {
                        Map<String, Object> notification = Map.of(
                                "action", action,
                                "personId", personId != null ? personId : -1L,
                                "timestamp", System.currentTimeMillis()
                        );
                        String json = objectMapper.writeValueAsString(notification);
                        session.getBasicRemote().sendText(json);
                    } catch (IOException e) {
                        System.err.println("Error sending message: " + e.getMessage());
                    }
                }
            }
        }
    }
}

