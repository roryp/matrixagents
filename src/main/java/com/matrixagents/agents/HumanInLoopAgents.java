package com.matrixagents.agents;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * Agents for the HUMAN-IN-THE-LOOP PATTERN.
 * Demonstrates interactive agent workflows requiring human input.
 * Pattern: Agent proposes -> Human reviews/approves -> Agent executes.
 */
public interface HumanInLoopAgents {

    /**
     * ProposalAgent: Creates proposals that require human approval.
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
        String createProposal(@V("request") String request);
    }

    /**
     * ExecutionAgent: Executes approved proposals with any human modifications.
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
        String execute(@V("proposal") String proposal, @V("feedback") String feedback);
    }

    /**
     * HoroscopeAgent: Generates horoscopes (used with human-in-loop for zodiac sign).
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
        String generateHoroscope(@V("sign") String sign);
    }

    /**
     * ZodiacExtractor: Tries to extract zodiac sign from user input.
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
        String extract(@V("input") String input);
    }
}
