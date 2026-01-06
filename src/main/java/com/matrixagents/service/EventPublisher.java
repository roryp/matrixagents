package com.matrixagents.service;

import com.matrixagents.model.AgentEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class EventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public EventPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publish(AgentEvent event) {
        messagingTemplate.convertAndSend("/topic/events", event);
        messagingTemplate.convertAndSend("/topic/patterns/" + event.patternName(), event);
    }

    public void publishToSession(String sessionId, AgentEvent event) {
        messagingTemplate.convertAndSendToUser(sessionId, "/queue/events", event);
    }
}
