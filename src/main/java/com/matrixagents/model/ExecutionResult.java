package com.matrixagents.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record ExecutionResult(
    String executionId,
    String patternId,
    String status,
    String result,
    List<AgentEvent> events,
    Map<String, Object> scopeSnapshot,
    Instant startTime,
    Instant endTime,
    long durationMs
) {
    public static ExecutionResult success(String executionId, String patternId, String result,
                                          List<AgentEvent> events, Map<String, Object> scopeSnapshot,
                                          Instant startTime) {
        Instant endTime = Instant.now();
        return new ExecutionResult(
            executionId,
            patternId,
            "COMPLETED",
            result,
            events,
            scopeSnapshot,
            startTime,
            endTime,
            endTime.toEpochMilli() - startTime.toEpochMilli()
        );
    }

    public static ExecutionResult error(String executionId, String patternId, String errorMessage,
                                        List<AgentEvent> events, Instant startTime) {
        Instant endTime = Instant.now();
        return new ExecutionResult(
            executionId,
            patternId,
            "ERROR",
            errorMessage,
            events,
            Map.of(),
            startTime,
            endTime,
            endTime.toEpochMilli() - startTime.toEpochMilli()
        );
    }

    public static ExecutionResult pending(String executionId, String patternId, String message,
                                          List<AgentEvent> events, Instant startTime) {
        return new ExecutionResult(
            executionId,
            patternId,
            "PENDING_HUMAN_INPUT",
            message,
            events,
            Map.of(),
            startTime,
            null,
            -1
        );
    }
}
