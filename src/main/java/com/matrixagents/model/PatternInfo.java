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
            List.of("CreativeWriter", "AudienceEditor", "StyleEditor"),
            Map.of("type", "SEQUENCE", "edges", List.of(
                Map.of("from", "CreativeWriter", "to", "AudienceEditor"),
                Map.of("from", "AudienceEditor", "to", "StyleEditor")
            )),
            "Write a fantasy story for teenagers in a humorous style"
        );
    }

    public static PatternInfo parallel() {
        return new PatternInfo(
            "parallel",
            "Parallel Workflow",
            "Multiple agents are invoked simultaneously, and their results are combined. Great for gathering diverse perspectives.",
            "workflow",
            List.of("FoodExpert", "MovieExpert"),
            Map.of("type", "PARALLEL", "edges", List.of(
                Map.of("from", "start", "to", "FoodExpert"),
                Map.of("from", "start", "to", "MovieExpert"),
                Map.of("from", "FoodExpert", "to", "combiner"),
                Map.of("from", "MovieExpert", "to", "combiner")
            )),
            "Plan a romantic evening with movie and meal suggestions"
        );
    }

    public static PatternInfo loop() {
        return new PatternInfo(
            "loop",
            "Loop Workflow",
            "Agents iterate until an exit condition is met. Perfect for refinement and quality improvement cycles.",
            "workflow",
            List.of("CreativeWriter", "StyleScorer", "StyleEditor"),
            Map.of("type", "LOOP", "maxIterations", 5, "edges", List.of(
                Map.of("from", "CreativeWriter", "to", "StyleScorer"),
                Map.of("from", "StyleScorer", "to", "StyleEditor"),
                Map.of("from", "StyleEditor", "to", "StyleScorer", "label", "iterate")
            )),
            "Write a story about dragons in a Shakespearean style"
        );
    }

    public static PatternInfo conditional() {
        return new PatternInfo(
            "conditional",
            "Conditional Routing",
            "Routes to different agents based on runtime conditions. Enables domain-specific expert selection.",
            "workflow",
            List.of("CategoryRouter", "MedicalExpert", "LegalExpert", "TechnicalExpert"),
            Map.of("type", "CONDITIONAL", "edges", List.of(
                Map.of("from", "CategoryRouter", "to", "MedicalExpert", "condition", "MEDICAL"),
                Map.of("from", "CategoryRouter", "to", "LegalExpert", "condition", "LEGAL"),
                Map.of("from", "CategoryRouter", "to", "TechnicalExpert", "condition", "TECHNICAL")
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
            List.of("BankSupervisor", "WithdrawAgent", "CreditAgent", "ExchangeAgent"),
            Map.of("type", "STAR", "edges", List.of(
                Map.of("from", "BankSupervisor", "to", "WithdrawAgent"),
                Map.of("from", "BankSupervisor", "to", "CreditAgent"),
                Map.of("from", "BankSupervisor", "to", "ExchangeAgent")
            )),
            "Transfer 100 USD from Mario to Georgios, then convert 50 USD to EUR"
        );
    }

    public static PatternInfo humanInLoop() {
        return new PatternInfo(
            "human-in-loop",
            "Human-in-the-Loop",
            "Pauses workflow execution to request human input or approval before proceeding.",
            "agentic",
            List.of("ProposalAgent", "Human", "ExecutionAgent"),
            Map.of("type", "SEQUENCE", "hasHuman", true, "edges", List.of(
                Map.of("from", "ProposalAgent", "to", "Human"),
                Map.of("from", "Human", "to", "ExecutionAgent")
            )),
            "Create a proposal for reorganizing the team structure"
        );
    }

    public static PatternInfo goap() {
        return new PatternInfo(
            "goap",
            "Goal-Oriented Planning (GOAP)",
            "Calculates the shortest path through agent dependencies to achieve a goal efficiently.",
            "planning",
            List.of("GoalPlanner", "PersonExtractor", "SignExtractor", "HoroscopeGenerator", "StoryFinder", "WriterAgent"),
            Map.of("type", "GOAP", "edges", List.of(
                Map.of("from", "GoalPlanner", "to", "PersonExtractor"),
                Map.of("from", "PersonExtractor", "to", "SignExtractor"),
                Map.of("from", "SignExtractor", "to", "HoroscopeGenerator"),
                Map.of("from", "SignExtractor", "to", "StoryFinder"),
                Map.of("from", "HoroscopeGenerator", "to", "WriterAgent"),
                Map.of("from", "StoryFinder", "to", "WriterAgent")
            )),
            "Generate a personalized horoscope and mythology for someone born on March 15th"
        );
    }

    public static PatternInfo p2p() {
        return new PatternInfo(
            "p2p",
            "Peer-to-Peer (P2P)",
            "Decentralized agent coordination where agents react to state changes and collaborate autonomously.",
            "planning",
            List.of("LiteratureAgent", "HypothesisAgent", "CriticAgent", "ValidationAgent", "ScorerAgent", "SynthesizerAgent"),
            Map.of("type", "P2P", "edges", List.of(
                Map.of("from", "LiteratureAgent", "to", "HypothesisAgent"),
                Map.of("from", "HypothesisAgent", "to", "CriticAgent"),
                Map.of("from", "CriticAgent", "to", "ValidationAgent"),
                Map.of("from", "ValidationAgent", "to", "ScorerAgent"),
                Map.of("from", "ScorerAgent", "to", "SynthesizerAgent")
            )),
            "Research the effects of caffeine on cognitive performance"
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
