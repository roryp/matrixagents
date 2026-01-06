package com.matrixagents.controller;

import com.matrixagents.model.AgentEvent;
import com.matrixagents.service.HumanInputService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class WebSocketController {

    private final HumanInputService humanInputService;

    public WebSocketController(HumanInputService humanInputService) {
        this.humanInputService = humanInputService;
    }

    @MessageMapping("/subscribe")
    @SendTo("/topic/events")
    public AgentEvent subscribe(SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        return AgentEvent.started("system", "Connected to Matrix Agents. Session: " + sessionId);
    }

    @MessageMapping("/human-input")
    public void handleHumanInput(@Payload Map<String, String> payload) {
        String requestId = payload.get("requestId");
        String input = payload.get("input");
        
        if (requestId != null && input != null) {
            humanInputService.provideInput(requestId, input);
        }
    }
}
