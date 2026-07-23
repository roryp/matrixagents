package com.matrixagents.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * Agents for the PARALLEL MAPPER (MAP-REDUCE) PATTERN using langchain4j-agentic 1.18.
 *
 * Unlike the plain parallel workflow (which runs DIFFERENT agents concurrently), the parallel
 * mapper fans a SINGLE sub-agent out over a COLLECTION of items, creating one agent instance per
 * item and running them all in parallel. The individual results are then aggregated into a list.
 *
 * Built via {@code AgenticServices.parallelMapperBuilder().subAgents(analyzer).itemsProvider("reviews")}.
 * Each item in the "reviews" collection is injected into the sub-agent's first argument.
 */
public interface ParallelMapperAgents {

    /**
     * ReviewAnalyzer: Analyzes ONE customer review. The mapper creates one instance of this agent
     * for every review in the batch and executes them concurrently.
     * Input: a single "review" item -> Output: a one-line sentiment analysis.
     */
    interface ReviewAnalyzer {
        @SystemMessage("""
            You are a customer-experience analyst. You classify the sentiment of a single product
            review and extract the single most important point the customer is making.
            """)
        @UserMessage("""
            Analyze the following customer review.
            Respond on a SINGLE line in exactly this format:
            <SENTIMENT> — <one short phrase summarizing the key point>
            where <SENTIMENT> is one of POSITIVE, NEGATIVE, or MIXED.

            Review: "{{review}}"
            """)
        @Agent(description = "Analyzes the sentiment and key point of a single customer review", outputKey = "analysis")
        String analyze(@V("review") String review);
    }
}
