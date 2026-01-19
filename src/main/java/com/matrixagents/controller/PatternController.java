package com.matrixagents.controller;

import com.matrixagents.model.*;
import com.matrixagents.service.PatternExecutionService;
import com.matrixagents.service.HumanInputService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PatternController {

    private final PatternExecutionService executionService;
    private final HumanInputService humanInputService;

    public PatternController(PatternExecutionService executionService,
                            HumanInputService humanInputService) {
        this.executionService = executionService;
        this.humanInputService = humanInputService;
    }

    @GET
    @Path("/patterns")
    public List<PatternInfo> getPatterns() {
        return PatternInfo.all();
    }

    @GET
    @Path("/patterns/{patternId}")
    public Response getPattern(@PathParam("patternId") String patternId) {
        return PatternInfo.all().stream()
                .filter(p -> p.id().equals(patternId))
                .findFirst()
                .map(Response::ok)
                .orElse(Response.status(Response.Status.NOT_FOUND))
                .build();
    }

    @POST
    @Path("/patterns/{patternId}/execute")
    public CompletableFuture<Response> executePattern(
            @PathParam("patternId") String patternId,
            ExecutionRequest request) {
        
        return executionService.executePattern(patternId, request.prompt())
                .thenApply(res -> Response.ok(res).build())
                .exceptionally(e -> Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(ExecutionResult.error(
                                java.util.UUID.randomUUID().toString(),
                                patternId,
                                e.getMessage(),
                                List.of(),
                                java.time.Instant.now()
                        )).build());
    }

    @POST
    @Path("/human-input/{requestId}")
    public Response provideHumanInput(
            @PathParam("requestId") String requestId,
            Map<String, String> body) {
        
        String input = body.get("input");
        if (input == null || input.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "Input is required")).build();
        }

        if (!humanInputService.hasPendingRequest(requestId)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        humanInputService.provideInput(requestId, input);
        return Response.ok(Map.of("status", "received", "requestId", requestId)).build();
    }

    @GET
    @Path("/human-input/pending")
    public Map<String, String> getPendingRequests() {
        return humanInputService.getPendingRequests();
    }

    @GET
    @Path("/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "service", "Matrix Agents Showcase",
                "patterns", PatternInfo.all().size(),
                "timestamp", java.time.Instant.now()
        );
    }
}
