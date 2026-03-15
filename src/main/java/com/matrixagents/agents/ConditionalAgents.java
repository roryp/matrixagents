package com.matrixagents.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * Agents for the CONDITIONAL PATTERN using langchain4j-agentic module.
 * Demonstrates routing to different agents based on classification.
 * Pattern: CategoryRouter classifies -> AgenticServices.conditionalBuilder() routes to expert.
 * 
 * Uses @Agent interfaces wired imperatively via AgenticServices.conditionalBuilder()
 * and AgenticServices.sequenceBuilder() in PatternExecutionService.
 */
public interface ConditionalAgents {

    /**
     * Category enum for routing decisions.
     */
    enum RequestCategory {
        MEDICAL, LEGAL, TECHNICAL, UNKNOWN
    }

    /**
     * CategoryRouter: Classifies user requests into categories.
     * Output key: "category" - used by activation conditions
     */
    interface CategoryRouter {
        @UserMessage("""
            Analyze the following user request and categorize it as 'legal', 'medical' or 'technical'.
            In case the request doesn't belong to any of those categories categorize it as 'unknown'.
            Reply with only one of those words and nothing else.
            The user request is: '{{request}}'.
            """)
        @Agent(description = "Categorizes a user request", outputKey = "category")
        RequestCategory classify(@V("request") String request);
    }

    /**
     * MedicalExpert: Provides medical-related information.
     * Activated when category == MEDICAL
     */
    interface MedicalExpert {
        @UserMessage("""
            You are a medical expert.
            Analyze the following user request under a medical point of view and provide the best possible answer.
            Be informative but emphasize that this is not medical advice.
            The user request is {{request}}.
            """)
        @Agent(description = "A medical expert", outputKey = "response")
        String medical(@V("request") String request);
    }

    /**
     * LegalExpert: Provides legal-related information.
     * Activated when category == LEGAL
     */
    interface LegalExpert {
        @UserMessage("""
            You are a legal expert.
            Analyze the following user request under a legal point of view and provide the best possible answer.
            Be informative but emphasize that this is not legal advice.
            The user request is {{request}}.
            """)
        @Agent(description = "A legal expert", outputKey = "response")
        String legal(@V("request") String request);
    }

    /**
     * TechnicalExpert: Provides technical/programming information.
     * Activated when category == TECHNICAL
     */
    interface TechnicalExpert {
        @UserMessage("""
            You are a technical expert.
            Analyze the following user request under a technical point of view and provide the best possible answer.
            Provide detailed, accurate technical information with code examples when relevant.
            The user request is {{request}}.
            """)
        @Agent(description = "A technical expert", outputKey = "response")
        String technical(@V("request") String request);
    }

    /**
     * ExpertRouterAgent: Typed interface for the full conditional workflow.
     * Wired via AgenticServices.sequenceBuilder() composing CategoryRouter
     * with a conditionalBuilder() that routes to the appropriate expert.
     */
    interface ExpertRouterAgent {
        @Agent
        String ask(@V("request") String request);
    }
}
