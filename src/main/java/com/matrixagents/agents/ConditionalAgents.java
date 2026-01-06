package com.matrixagents.agents;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * Agents for the CONDITIONAL PATTERN.
 * Demonstrates routing to different agents based on classification.
 * Pattern: Router classifies -> Conditional expert activation.
 */
public interface ConditionalAgents {

    /**
     * Category enum for routing decisions.
     */
    enum RequestCategory {
        MEDICAL, LEGAL, TECHNICAL, GENERAL
    }

    /**
     * CategoryRouter: Classifies user requests into categories.
     */
    interface CategoryRouter {
        @SystemMessage("""
            You are a request classifier. Analyze the user's query and classify it
            into exactly ONE of these categories:
            
            - MEDICAL: Health, symptoms, medications, treatments, medical conditions
            - LEGAL: Laws, contracts, rights, regulations, legal processes
            - TECHNICAL: Programming, software, hardware, IT systems, technology
            - GENERAL: Everything else
            
            IMPORTANT: Respond with ONLY the category name in uppercase.
            No explanations, just the single word: MEDICAL, LEGAL, TECHNICAL, or GENERAL
            """)
        @UserMessage("Classify this request: {{request}}")
        String classify(@V("request") String request);
    }

    /**
     * MedicalExpert: Provides medical-related information.
     */
    interface MedicalExpert {
        @SystemMessage("""
            You are a medical information assistant. Provide helpful health information
            while always recommending consultation with healthcare professionals.
            Be informative but emphasize that this is not medical advice.
            """)
        @UserMessage("{{request}}")
        String answer(@V("request") String request);
    }

    /**
     * LegalExpert: Provides legal-related information.
     */
    interface LegalExpert {
        @SystemMessage("""
            You are a legal information assistant. Provide helpful legal information
            while always recommending consultation with qualified attorneys.
            Be informative but emphasize that this is not legal advice.
            """)
        @UserMessage("{{request}}")
        String answer(@V("request") String request);
    }

    /**
     * TechnicalExpert: Provides technical/programming information.
     */
    interface TechnicalExpert {
        @SystemMessage("""
            You are a technical expert specializing in software, programming, and IT.
            Provide detailed, accurate technical information with code examples when relevant.
            Explain concepts clearly for different skill levels.
            """)
        @UserMessage("{{request}}")
        String answer(@V("request") String request);
    }

    /**
     * GeneralExpert: Handles general queries.
     */
    interface GeneralExpert {
        @SystemMessage("""
            You are a helpful general assistant. Provide informative, friendly responses
            to a wide variety of questions and requests.
            """)
        @UserMessage("{{request}}")
        String answer(@V("request") String request);
    }
}
