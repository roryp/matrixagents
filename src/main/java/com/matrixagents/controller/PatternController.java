package com.matrixagents.controller;

import com.matrixagents.model.*;
import com.matrixagents.service.PatternExecutionService;
import com.matrixagents.service.HumanInputService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class PatternController {

    private final PatternExecutionService executionService;
    private final HumanInputService humanInputService;

    public PatternController(PatternExecutionService executionService,
                            HumanInputService humanInputService) {
        this.executionService = executionService;
        this.humanInputService = humanInputService;
    }

    @GetMapping("/patterns")
    public ResponseEntity<List<PatternInfo>> getPatterns() {
        return ResponseEntity.ok(PatternInfo.all());
    }

    @GetMapping("/patterns/{patternId}")
    public ResponseEntity<PatternInfo> getPattern(@PathVariable String patternId) {
        return PatternInfo.all().stream()
                .filter(p -> p.id().equals(patternId))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/patterns/{patternId}/execute")
    public CompletableFuture<ResponseEntity<ExecutionResult>> executePattern(
            @PathVariable String patternId,
            @RequestBody ExecutionRequest request) {
        
        return executionService.executePattern(patternId, request.prompt())
                .thenApply(ResponseEntity::ok)
                .exceptionally(e -> ResponseEntity.internalServerError()
                        .body(ExecutionResult.error(
                                java.util.UUID.randomUUID().toString(),
                                patternId,
                                e.getMessage(),
                                List.of(),
                                java.time.Instant.now()
                        )));
    }

    @PostMapping("/human-input/{requestId}")
    public ResponseEntity<Map<String, String>> provideHumanInput(
            @PathVariable String requestId,
            @RequestBody Map<String, String> body) {
        
        String input = body.get("input");
        if (input == null || input.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Input is required"));
        }

        if (!humanInputService.hasPendingRequest(requestId)) {
            return ResponseEntity.notFound().build();
        }

        humanInputService.provideInput(requestId, input);
        return ResponseEntity.ok(Map.of("status", "received", "requestId", requestId));
    }

    @GetMapping("/human-input/pending")
    public ResponseEntity<Map<String, String>> getPendingRequests() {
        return ResponseEntity.ok(humanInputService.getPendingRequests());
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Matrix Agents Showcase",
                "patterns", PatternInfo.all().size(),
                "timestamp", java.time.Instant.now()
        ));
    }
}
