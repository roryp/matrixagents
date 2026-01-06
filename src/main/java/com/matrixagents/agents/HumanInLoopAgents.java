package com.matrixagents.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * Agents for the HUMAN-IN-THE-LOOP PATTERN using langchain4j-agentic module.
 * Demonstrates interactive agent workflows requiring human input.
 * Pattern: Agent proposes -> Human reviews/approves -> Agent executes.
 * 
 * Uses @Agent annotation for proper agent orchestration with human intervention points.
 */
public interface HumanInLoopAgents {

    /**
     * ProposalAgent: Creates proposals that require human approval.
     * Output key: "proposal" - awaits human review
     */
    interface ProposalAgent {
        @SystemMessage("""
            You are a proposal generator that creates detailed action plans.
            Based on the user's request, create a clear proposal with:
            
            1. SUMMARY: Brief description of what will be done
            2. STEPS: Numbered list of specific actions
            3. RISKS: Potential issues or concerns
            4. BENEFITS: Expected outcomes
            
            The proposal will be reviewed by a human before execution.
            Be thorough but concise.
            """)
        @UserMessage("Create a proposal for: {{request}}")
        @Agent(description = "Creates proposals that require human approval", outputKey = "proposal")
        String createProposal(@V("request") String request);
    }

    /**
     * ExecutionAgent: Executes approved proposals with any human modifications.
     * Input: "proposal" and "feedback" from human review
     * Output key: "executionResult"
     */
    interface ExecutionAgent {
        @SystemMessage("""
            You are an execution agent that carries out approved proposals.
            The human has reviewed the proposal and may have provided feedback.
            
            Execute the proposal considering any modifications or feedback.
            Provide a detailed execution report including:
            
            1. ACTIONS TAKEN: What was done
            2. MODIFICATIONS: Any changes based on feedback
            3. RESULTS: Outcomes of each action
            4. STATUS: Overall success/completion status
            """)
        @UserMessage("""
            Execute this approved proposal:
            
            PROPOSAL:
            {{proposal}}
            
            HUMAN FEEDBACK:
            {{feedback}}
            """)
        @Agent(description = "Executes approved proposals with human feedback", outputKey = "executionResult")
        String execute(@V("proposal") String proposal, @V("feedback") String feedback);
    }

    /**
     * HoroscopeAgent: Generates horoscopes (used with human-in-loop for zodiac sign).
     * Input: "sign" (may come from human input)
     * Output key: "horoscope"
     */
    interface HoroscopeAgent {
        @SystemMessage("""
            You are a mystical astrologer who creates personalized horoscopes.
            Generate an engaging, positive horoscope for the given zodiac sign.
            Include insights about:
            - Love and relationships
            - Career and finances
            - Health and wellness
            - Lucky numbers and colors
            
            Make it feel personal and uplifting.
            """)
        @UserMessage("Generate a horoscope for {{sign}}:")
        @Agent(description = "Generates personalized horoscopes for zodiac signs", outputKey = "horoscope")
        String generateHoroscope(@V("sign") String sign);
    }

    /**
     * ZodiacExtractor: Tries to extract zodiac sign from user input.
     * Output key: "extractedSign" - may trigger human input if UNKNOWN
     */
    interface ZodiacExtractor {
        @SystemMessage("""
            You are a zodiac sign extractor. Analyze the user's message and determine
            if they mentioned a zodiac sign directly or provided a birth date.
            
            If a zodiac sign is found or can be determined: respond with just the sign name.
            If birth date is given: calculate and respond with the zodiac sign.
            If neither is found: respond with "UNKNOWN"
            
            Valid signs: Aries, Taurus, Gemini, Cancer, Leo, Virgo, Libra, Scorpio, 
            Sagittarius, Capricorn, Aquarius, Pisces
            """)
        @UserMessage("{{input}}")
        @Agent(description = "Extracts zodiac sign from user input", outputKey = "extractedSign")
        String extract(@V("input") String input);
    }

    /**
     * HoroscopeWorkflow: Typed interface for the human-in-the-loop workflow.
     * The workflow pauses for human input when zodiac sign is unknown.
     */
    interface HoroscopeWorkflow {
        @Agent
        String getHoroscope(@V("input") String input);
    }
}
