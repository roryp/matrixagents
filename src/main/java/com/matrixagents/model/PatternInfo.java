package com.matrixagents.model;

import java.util.List;
import java.util.Map;

public record PatternInfo(
    String id,
    String name,
    String description,
    String category,
    List<String> agents,
    Map<String, Object> topology,
    String examplePrompt
) {
    public static PatternInfo sequence() {
        return new PatternInfo(
            "sequence",
            "Sequential Workflow",
            "Agents are invoked one after another in a predefined order. Each agent's output can be used as input for the next.",
            "workflow",
            List.of("researcher", "analyzer", "writer"),
            Map.of("type", "SEQUENCE", "edges", List.of(
                Map.of("from", "researcher", "to", "analyzer"),
                Map.of("from", "analyzer", "to", "writer")
            )),
            "Write a comprehensive article about quantum computing"
        );
    }

    public static PatternInfo parallel() {
        return new PatternInfo(
            "parallel",
            "Parallel Workflow",
            "Multiple agents are invoked simultaneously, and their results are combined. Great for gathering diverse perspectives.",
            "workflow",
            List.of("techExpert", "businessExpert", "creativeExpert"),
            Map.of("type", "PARALLEL", "edges", List.of(
                Map.of("from", "start", "to", "techExpert"),
                Map.of("from", "start", "to", "businessExpert"),
                Map.of("from", "start", "to", "creativeExpert"),
                Map.of("from", "techExpert", "to", "combiner"),
                Map.of("from", "businessExpert", "to", "combiner"),
                Map.of("from", "creativeExpert", "to", "combiner")
            )),
            "Analyze the impact of AI on the job market"
        );
    }

    public static PatternInfo loop() {
        return new PatternInfo(
            "loop",
            "Loop Workflow",
            "Agents iterate until an exit condition is met. Perfect for refinement and quality improvement cycles.",
            "workflow",
            List.of("generator", "critic", "refiner"),
            Map.of("type", "LOOP", "maxIterations", 5, "edges", List.of(
                Map.of("from", "generator", "to", "critic"),
                Map.of("from", "critic", "to", "refiner"),
                Map.of("from", "refiner", "to", "generator", "label", "iterate")
            )),
            "Create a perfect elevator pitch for a startup"
        );
    }

    public static PatternInfo conditional() {
        return new PatternInfo(
            "conditional",
            "Conditional Routing",
            "Routes to different agents based on runtime conditions. Enables domain-specific expert selection.",
            "workflow",
            List.of("router", "medicalExpert", "legalExpert", "techExpert"),
            Map.of("type", "CONDITIONAL", "edges", List.of(
                Map.of("from", "router", "to", "medicalExpert", "condition", "medical"),
                Map.of("from", "router", "to", "legalExpert", "condition", "legal"),
                Map.of("from", "router", "to", "techExpert", "condition", "technical")
            )),
            "What are the symptoms of diabetes and available treatments?"
        );
    }

    public static PatternInfo supervisor() {
        return new PatternInfo(
            "supervisor",
            "Supervisor Agent",
            "An LLM-based supervisor autonomously plans and orchestrates sub-agents to complete complex tasks.",
            "agentic",
            List.of("supervisor", "researcher", "calculator", "writer"),
            Map.of("type", "STAR", "edges", List.of(
                Map.of("from", "supervisor", "to", "researcher"),
                Map.of("from", "supervisor", "to", "calculator"),
                Map.of("from", "supervisor", "to", "writer")
            )),
            "Research current stock prices for AAPL and MSFT, calculate their average, and write a brief analysis"
        );
    }

    public static PatternInfo humanInLoop() {
        return new PatternInfo(
            "human-in-loop",
            "Human-in-the-Loop",
            "Pauses workflow execution to request human input or approval before proceeding.",
            "agentic",
            List.of("proposer", "human", "executor"),
            Map.of("type", "SEQUENCE", "hasHuman", true, "edges", List.of(
                Map.of("from", "proposer", "to", "human"),
                Map.of("from", "human", "to", "executor")
            )),
            "Draft and send an important email to the CEO"
        );
    }

    public static PatternInfo goap() {
        return new PatternInfo(
            "goap",
            "Goal-Oriented Planning (GOAP)",
            "Calculates the shortest path through agent dependencies to achieve a goal efficiently.",
            "planning",
            List.of("dataCollector", "analyzer", "visualizer", "reporter"),
            Map.of("type", "GOAP", "edges", List.of(
                Map.of("from", "dataCollector", "to", "analyzer"),
                Map.of("from", "analyzer", "to", "visualizer"),
                Map.of("from", "analyzer", "to", "reporter"),
                Map.of("from", "visualizer", "to", "reporter")
            )),
            "Generate a comprehensive sales report with visualizations"
        );
    }

    public static PatternInfo p2p() {
        return new PatternInfo(
            "p2p",
            "Peer-to-Peer (P2P)",
            "Decentralized agent coordination where agents react to state changes and collaborate autonomously.",
            "planning",
            List.of("ideaGenerator", "critic", "validator", "scorer"),
            Map.of("type", "P2P", "edges", List.of(
                Map.of("from", "ideaGenerator", "to", "critic", "bidirectional", true),
                Map.of("from", "critic", "to", "validator", "bidirectional", true),
                Map.of("from", "validator", "to", "scorer", "bidirectional", true)
            )),
            "Brainstorm and validate innovative solutions for climate change"
        );
    }

    public static List<PatternInfo> all() {
        return List.of(
            sequence(),
            parallel(),
            loop(),
            conditional(),
            supervisor(),
            humanInLoop(),
            goap(),
            p2p()
        );
    }
}
