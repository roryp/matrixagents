package com.matrixagents.model;

public record ExecutionRequest(
    String patternId,
    String prompt,
    java.util.Map<String, Object> parameters
) {
    public ExecutionRequest {
        if (patternId == null || patternId.isBlank()) {
            throw new IllegalArgumentException("patternId is required");
        }
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("prompt is required");
        }
        if (parameters == null) {
            parameters = java.util.Map.of();
        }
    }
}
