package com.matrixagents.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.declarative.ActivationCondition;
import dev.langchain4j.agentic.declarative.ConditionalAgent;
import dev.langchain4j.agentic.declarative.SequenceAgent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * Agents for the CONDITIONAL PATTERN using langchain4j-agentic module.
 * Demonstrates routing to different agents based on classification.
 * Pattern: CategoryRouter classifies -> Conditional expert activation.
 * 
 * Uses @Agent with @ConditionalAgent and @ActivationCondition for routing.
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
     * ExpertRouterAgent: Typed interface for the conditional workflow.
     * Routes to appropriate expert based on category classification.
     */
    interface ExpertRouterAgent {
        @ConditionalAgent(outputKey = "response", subAgents = {MedicalExpert.class, TechnicalExpert.class, LegalExpert.class})
        String askExpert(@V("request") String request);

        @ActivationCondition(MedicalExpert.class)
        static boolean activateMedical(@V("category") RequestCategory category) {
            return category == RequestCategory.MEDICAL;
        }

        @ActivationCondition(TechnicalExpert.class)
        static boolean activateTechnical(@V("category") RequestCategory category) {
            return category == RequestCategory.TECHNICAL;
        }

        @ActivationCondition(LegalExpert.class)
        static boolean activateLegal(@V("category") RequestCategory category) {
            return category == RequestCategory.LEGAL;
        }
    }

    /**
     * ExpertChatbot: Combines CategoryRouter and ExpertRouterAgent in a sequence.
     * This is the typed interface for the full conditional workflow.
     * The sequence ensures both agents share the same AgenticScope.
     * Uses @SequenceAgent to declaratively compose CategoryRouter (classifier) with ExpertRouterAgent (conditional router).
     */
    interface ExpertChatbot {
        @SequenceAgent(outputKey = "response", subAgents = {CategoryRouter.class, ExpertRouterAgent.class})
        String ask(@V("request") String request);
    }
}
