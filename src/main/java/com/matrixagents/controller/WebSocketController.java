package com.matrixagents.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matrixagents.model.AgentEvent;
import com.matrixagents.service.EventPublisher;
import com.matrixagents.service.HumanInputService;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

@ServerEndpoint("/ws")
public class WebSocketController {

    private static final Logger log = LoggerFactory.getLogger(WebSocketController.class);

    @Inject
    HumanInputService humanInputService;

    @Inject
    EventPublisher eventPublisher;

    @Inject
    ObjectMapper objectMapper;

    @OnOpen
    public void onOpen(Session session) {
        log.info("WebSocket opened: {}", session.getId());
        eventPublisher.addSession(session);
        AgentEvent event = AgentEvent.started("system", "Connected to Matrix Agents. Session: " + session.getId());
        eventPublisher.publishToSession(session.getId(), event);
    }

    @OnClose
    public void onClose(Session session) {
        log.info("WebSocket closed: {}", session.getId());
        eventPublisher.removeSession(session);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        log.error("WebSocket error for session {}", session.getId(), throwable);
        eventPublisher.removeSession(session);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            Map<String, String> payload = objectMapper.readValue(message, Map.class);
            String type = payload.get("type");
            
            if ("human-input".equals(type)) {
                String requestId = payload.get("requestId");
                String input = payload.get("input");
                
                if (requestId != null && input != null) {
                    humanInputService.provideInput(requestId, input);
                }
            } else if ("subscribe".equals(type)) {
                // Subscription is handled by being connected in this simple implementation
                AgentEvent event = AgentEvent.started("system", "Subscribed to events. Session: " + session.getId());
                eventPublisher.publishToSession(session.getId(), event);
            }
        } catch (IOException e) {
            log.error("Error processing WebSocket message", e);
        }
    }
}
