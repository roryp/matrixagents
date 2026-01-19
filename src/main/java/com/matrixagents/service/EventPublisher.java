package com.matrixagents.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.matrixagents.model.AgentEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    @Inject
    ObjectMapper objectMapper;

    public void addSession(Session session) {
        sessions.put(session.getId(), session);
    }

    public void removeSession(Session session) {
        sessions.remove(session.getId());
    }

    public void publish(AgentEvent event) {
        String message;
        try {
            message = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize AgentEvent", e);
            return;
        }

        sessions.values().forEach(session -> {
            if (session.isOpen()) {
                session.getAsyncRemote().sendText(message);
            }
        });
    }

    public void publishToSession(String sessionId, AgentEvent event) {
        Session session = sessions.get(sessionId);
        if (session != null && session.isOpen()) {
            try {
                String message = objectMapper.writeValueAsString(event);
                session.getAsyncRemote().sendText(message);
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize AgentEvent for session {}", sessionId, e);
            }
        }
    }
}
