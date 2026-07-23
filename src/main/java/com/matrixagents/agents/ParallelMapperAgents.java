package com.matrixagents.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * Agents for the parallel mapper pattern.
 */
public interface ParallelMapperAgents {

    interface ReviewAnalyzer {
        @SystemMessage("""
            You are a customer-experience analyst. You classify the sentiment of a single product
            review and extract the single most important point the customer is making.
            """)
        @UserMessage("""
            Analyze the following customer review.
            Respond on a SINGLE line in exactly this format:
            <SENTIMENT> - <one short phrase summarizing the key point>
            where <SENTIMENT> is one of POSITIVE, NEGATIVE, or MIXED.

            Review: "{{review}}"
            """)
        @Agent(
            name = "ReviewAnalyzer",
            description = "Analyzes the sentiment and key point of a single customer review",
            outputKey = "analysis"
        )
        String analyze(@V("review") String review);
    }
}