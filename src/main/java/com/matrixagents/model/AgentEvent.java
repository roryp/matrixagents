package com.matrixagents.model;

import java.time.Instant;
import java.util.Map;

public record AgentEvent(
    String eventId,
    String patternName,
    String agentName,
    EventType eventType,
    String message,
    Map<String, Object> data,
    Instant timestamp
) {
    public enum EventType {
        STARTED,
        AGENT_INVOKED,
        AGENT_COMPLETED,
        STATE_UPDATED,
        HUMAN_INPUT_REQUIRED,
        HUMAN_INPUT_RECEIVED,
        ERROR,
        COMPLETED
    }

    public static AgentEvent started(String patternName, String message) {
        return new AgentEvent(
            java.util.UUID.randomUUID().toString(),
            patternName,
            null,
            EventType.STARTED,
            message,
            Map.of(),
            Instant.now()
        );
    }

    public static AgentEvent agentInvoked(String patternName, String agentName, String message) {
        return new AgentEvent(
            java.util.UUID.randomUUID().toString(),
            patternName,
            agentName,
            EventType.AGENT_INVOKED,
            message,
            Map.of(),
            Instant.now()
        );
    }

    public static AgentEvent agentCompleted(String patternName, String agentName, String result) {
        return new AgentEvent(
            java.util.UUID.randomUUID().toString(),
            patternName,
            agentName,
            EventType.AGENT_COMPLETED,
            result,
            Map.of("result", result),
            Instant.now()
        );
    }

    public static AgentEvent stateUpdated(String patternName, String key, Object value) {
        return new AgentEvent(
            java.util.UUID.randomUUID().toString(),
            patternName,
            null,
            EventType.STATE_UPDATED,
            "State updated: " + key,
            Map.of("key", key, "value", value),
            Instant.now()
        );
    }

    public static AgentEvent humanInputRequired(String patternName, String prompt, String requestId) {
        return new AgentEvent(
            java.util.UUID.randomUUID().toString(),
            patternName,
            "human",
            EventType.HUMAN_INPUT_REQUIRED,
            prompt,
            Map.of("requestId", requestId),
            Instant.now()
        );
    }

    public static AgentEvent completed(String patternName, String result) {
        return new AgentEvent(
            java.util.UUID.randomUUID().toString(),
            patternName,
            null,
            EventType.COMPLETED,
            result,
            Map.of("finalResult", result),
            Instant.now()
        );
    }

    public static AgentEvent error(String patternName, String agentName, String errorMessage) {
        return new AgentEvent(
            java.util.UUID.randomUUID().toString(),
            patternName,
            agentName,
            EventType.ERROR,
            errorMessage,
            Map.of(),
            Instant.now()
        );
    }
}
