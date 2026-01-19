package com.matrixagents.service;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;

@ApplicationScoped
public class HumanInputService {

    private final Map<String, CompletableFuture<String>> pendingRequests = new ConcurrentHashMap<>();
    private final Map<String, String> pendingPrompts = new ConcurrentHashMap<>();

    public CompletableFuture<String> requestInput(String requestId, String prompt) {
        CompletableFuture<String> future = new CompletableFuture<>();
        pendingRequests.put(requestId, future);
        pendingPrompts.put(requestId, prompt);
        return future;
    }

    public void provideInput(String requestId, String input) {
        CompletableFuture<String> future = pendingRequests.remove(requestId);
        pendingPrompts.remove(requestId);
        if (future != null) {
            future.complete(input);
        }
    }

    public Map<String, String> getPendingRequests() {
        return Map.copyOf(pendingPrompts);
    }

    public boolean hasPendingRequest(String requestId) {
        return pendingRequests.containsKey(requestId);
    }

    public void cancelRequest(String requestId) {
        CompletableFuture<String> future = pendingRequests.remove(requestId);
        pendingPrompts.remove(requestId);
        if (future != null) {
            future.cancel(true);
        }
    }
}
