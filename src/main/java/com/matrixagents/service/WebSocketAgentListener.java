package com.matrixagents.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.matrixagents.model.AgentEvent;

import dev.langchain4j.agentic.observability.AgentInvocationError;
import dev.langchain4j.agentic.observability.AgentListener;
import dev.langchain4j.agentic.observability.AgentRequest;
import dev.langchain4j.agentic.observability.AgentResponse;
import dev.langchain4j.agentic.scope.AgenticScope;

/**
 * AgentListener implementation that publishes real-time events to WebSocket clients.
 * This is the proper LangChain4j way to observe agent executions instead of manual loops.
 * 
 * Usage:
 * AgenticServices.loopBuilder()
 *     .listener(new WebSocketAgentListener(eventPublisher, "loop", events))
 *     .build();
 */
public class WebSocketAgentListener implements AgentListener {
    
    private static final Logger log = LoggerFactory.getLogger(WebSocketAgentListener.class);
    
    private final EventPublisher eventPublisher;
    private final String patternId;
    private final List<AgentEvent> events;
    private final Map<String, Object> scopeSnapshot;
    
    public WebSocketAgentListener(EventPublisher eventPublisher, String patternId, List<AgentEvent> events) {
        this.eventPublisher = eventPublisher;
        this.patternId = patternId;
        this.events = events;
        this.scopeSnapshot = new ConcurrentHashMap<>();
    }
    
    /**
     * Returns a snapshot of the scope state captured during agent invocations.
     */
    public Map<String, Object> getScopeSnapshot() {
        return Collections.unmodifiableMap(scopeSnapshot);
    }
    
    @Override
    public void beforeAgentInvocation(AgentRequest request) {
        String agentName = request.agent().name();
        Map<String, Object> inputs = request.inputs();
        
        log.debug("Agent {} invoked with inputs: {}", agentName, inputs);
        
        String description = formatInputs(inputs);
        AgentEvent event = AgentEvent.agentInvoked(patternId, agentName, description);
        events.add(event);
        eventPublisher.publish(event);
    }
    
    @Override
    public void afterAgentInvocation(AgentResponse response) {
        String agentName = response.agent().name();
        Object output = response.output();
        AgenticScope scope = response.agenticScope();
        
        log.debug("Agent {} completed with output: {}", agentName, truncate(String.valueOf(output)));
        
        // Capture scope state
        captureScope(scope);
        
        // Publish completion event
        String outputStr = truncate(String.valueOf(output));
        AgentEvent event = AgentEvent.agentCompleted(patternId, agentName, outputStr);
        events.add(event);
        eventPublisher.publish(event);
        
        // Publish state update for key outputs
        String outputKey = response.agent().outputKey();
        if (outputKey != null && output != null) {
            AgentEvent stateEvent = AgentEvent.stateUpdated(patternId, outputKey, truncate(String.valueOf(output)));
            events.add(stateEvent);
            eventPublisher.publish(stateEvent);
        }
    }
    
    @Override
    public void onAgentInvocationError(AgentInvocationError error) {
        String agentName = error.agent().name();
        String errorMessage = error.error().getMessage();
        
        log.error("Agent {} failed: {}", agentName, errorMessage, error.error());
        
        AgentEvent event = AgentEvent.error(patternId, agentName, errorMessage);
        events.add(event);
        eventPublisher.publish(event);
    }
    
    @Override
    public void afterAgenticScopeCreated(AgenticScope scope) {
        log.debug("AgenticScope created with memoryId: {}", scope.memoryId());
        captureScope(scope);
    }
    
    @Override
    public void beforeAgenticScopeDestroyed(AgenticScope scope) {
        log.debug("AgenticScope being destroyed, final state capture");
        captureScope(scope);
    }
    
    @Override
    public boolean inheritedBySubagents() {
        // We want to observe ALL agents in the hierarchy
        return true;
    }
    
    /**
     * Capture all state from the AgenticScope for return in ExecutionResult.
     */
    private void captureScope(AgenticScope scope) {
        if (scope != null) {
            Map<String, Object> state = scope.state();
            if (state != null) {
                scopeSnapshot.putAll(state);
            }
        }
    }
    
    private String formatInputs(Map<String, Object> inputs) {
        if (inputs == null || inputs.isEmpty()) {
            return "Processing...";
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : inputs.entrySet()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(entry.getKey()).append("=").append(truncate(String.valueOf(entry.getValue())));
        }
        return sb.toString();
    }
    
    private String truncate(String text) {
        if (text == null) return "";
        return text.length() > 200 ? text.substring(0, 200) + "..." : text;
    }
}
