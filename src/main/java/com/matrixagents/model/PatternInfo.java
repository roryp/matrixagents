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
            List.of("ZodiacExtractor", "Human", "HoroscopeAgent"),
            Map.of("type", "SEQUENCE", "hasHuman", true, "edges", List.of(
                Map.of("from", "ZodiacExtractor", "to", "Human"),
                Map.of("from", "Human", "to", "HoroscopeAgent")
            )),
            "Create a horoscope for me"
        );
    }

    public static PatternInfo goap() {
        return new PatternInfo(
            "goap",
            "Goal-Oriented Planning (GOAP)",
            "Solves the Travelling Salesman Problem by planning optimal routes through agent dependency graphs.",
            "planning",
            List.of("CityParser", "DistanceCalculator", "AttractionFinder", "RouteOptimizer", "ItineraryPlanner"),
            Map.of("type", "GOAP", "edges", List.of(
                Map.of("from", "CityParser", "to", "DistanceCalculator"),
                Map.of("from", "CityParser", "to", "AttractionFinder"),
                Map.of("from", "DistanceCalculator", "to", "RouteOptimizer"),
                Map.of("from", "RouteOptimizer", "to", "ItineraryPlanner"),
                Map.of("from", "AttractionFinder", "to", "ItineraryPlanner")
            )),
            "Plan a trip visiting Paris, London, Rome, Berlin and Barcelona"
        );
    }

    public static PatternInfo p2p() {
        return new PatternInfo(
            "p2p",
            "Peer-to-Peer (P2P)",
            "Decentralized agent coordination where agents react to state changes and collaborate autonomously. Runs until consensus score reaches 0.75 threshold.",
            "planning",
            List.of("LiteratureAgent", "HypothesisAgent", "CriticAgent", "ValidationAgent", "ScorerAgent"),
            Map.of("type", "P2P", "edges", List.of(
                Map.of("from", "LiteratureAgent", "to", "HypothesisAgent"),
                Map.of("from", "HypothesisAgent", "to", "CriticAgent"),
                Map.of("from", "HypothesisAgent", "to", "ScorerAgent"),
                Map.of("from", "CriticAgent", "to", "ValidationAgent"),
                Map.of("from", "ValidationAgent", "to", "HypothesisAgent")
            )),
            "Research the effects of caffeine on cognitive performance"
        );
    }

    public static PatternInfo parallelMapper() {
        return new PatternInfo(
            "parallel-mapper",
            "Parallel Mapper (Map-Reduce)",
            "Fans a single agent out over a collection, running one instance per item concurrently, then aggregates the results into a list. Perfect for batch processing like analyzing many reviews at once.",
            "workflow",
            List.of("ReviewAnalyzer"),
            Map.of("type", "PARALLEL", "edges", List.of(
                Map.of("from", "start", "to", "ReviewAnalyzer"),
                Map.of("from", "ReviewAnalyzer", "to", "combiner")
            )),
            "The battery easily lasts two days and charges fast\nThe screen cracked after a single short drop\nGreat value for the price, would buy again\nSupport never responded to my emails\nSetup was confusing but it works well now"
        );
    }

    public static PatternInfo debate() {
        return new PatternInfo(
            "debate",
            "Debate",
            "Debater agents argue a question from opposing viewpoints across parallel rounds, reading and refining against each other's reasoning, until they converge or a judge renders the final verdict.",
            "planning",
            List.of("Judge", "Proponent", "Skeptic", "Pragmatist"),
            Map.of("type", "STAR", "edges", List.of(
                Map.of("from", "Proponent", "to", "Judge"),
                Map.of("from", "Skeptic", "to", "Judge"),
                Map.of("from", "Pragmatist", "to", "Judge")
            )),
            "Should companies adopt a four-day work week?"
        );
    }

    public static PatternInfo voting() {
        return new PatternInfo(
            "voting",
            "Voting",
            "An ensemble of independent analyst agents each vote on the same question in parallel, and a voting strategy aggregates the ballots into a single majority decision.",
            "planning",
            List.of("GrowthAnalyst", "ValueAnalyst", "RiskAnalyst"),
            Map.of("type", "PARALLEL", "edges", List.of(
                Map.of("from", "start", "to", "GrowthAnalyst"),
                Map.of("from", "start", "to", "ValueAnalyst"),
                Map.of("from", "start", "to", "RiskAnalyst"),
                Map.of("from", "GrowthAnalyst", "to", "combiner"),
                Map.of("from", "ValueAnalyst", "to", "combiner"),
                Map.of("from", "RiskAnalyst", "to", "combiner")
            )),
            "Evaluate an investment in a fast-growing EV startup: revenue up 45% year-over-year, but heavy debt and no profits yet."
        );
    }

    public static List<PatternInfo> all() {
        return List.of(
            sequence(),
            parallel(),
            parallelMapper(),
            loop(),
            conditional(),
            supervisor(),
            humanInLoop(),
            goap(),
            p2p(),
            debate(),
            voting()
        );
    }
}
