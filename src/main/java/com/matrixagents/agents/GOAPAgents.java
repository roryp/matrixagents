package com.matrixagents.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * Agents for the GOAP (Goal-Oriented Action Planning) PATTERN using langchain4j-agentic module.
 * 
 * GOAP uses GoalOrientedPlanner which automatically:
 * 1. Builds a dependency graph from agent input/output keys
 * 2. Calculates the shortest path from current state to goal
 * 3. Executes agents in the computed sequence
 * 
 * Key: Each agent's @V parameters define inputs, outputKey defines output.
 * The planner uses these to build the dependency graph.
 * 
 * Dependency Graph:
 *   prompt -> sign (via SignExtractor)
 *   sign -> horoscope (via HoroscopeGenerator)
 *   sign -> story (via StoryFinder)
 *   horoscope, story -> writeup (via WriterAgent)
 */
public interface GOAPAgents {

    /**
     * SignExtractor: Extracts zodiac sign from the prompt.
     * Input: prompt -> Output: sign
     */
    interface SignExtractor {
        @SystemMessage("""
            You are a zodiac expert. Extract or determine the zodiac sign from the text.
            Look for:
            - Directly mentioned zodiac signs (Aries, Taurus, Gemini, Cancer, Leo, Virgo, Libra, Scorpio, Sagittarius, Capricorn, Aquarius, Pisces)
            - Birth dates that can be converted to zodiac signs
            
            Return ONLY the zodiac sign name (e.g., "Scorpio", "Leo").
            If no sign can be determined, return "Aries".
            """)
        @UserMessage("What zodiac sign is mentioned or can be determined from: {{prompt}}")
        @Agent("Extract zodiac sign from user's prompt")
        String extractSign(@V("prompt") String prompt);
    }

    /**
     * HoroscopeGenerator: Creates a horoscope for a zodiac sign.
     * Input: sign -> Output: horoscope
     */
    interface HoroscopeGenerator {
        @SystemMessage("""
            You are an astrologer. Generate a detailed, personalized horoscope.
            Include predictions for love, career, health, and general fortune.
            Make it engaging and positive while feeling authentic.
            Keep the response concise (3-4 paragraphs).
            """)
        @UserMessage("Generate a horoscope for someone who is a {{sign}}:")
        @Agent("Generate horoscope based on zodiac sign")
        String generateHoroscope(@V("sign") String sign);
    }

    /**
     * StoryFinder: Finds mythology and stories related to the zodiac sign.
     * Input: sign -> Output: story
     */
    interface StoryFinder {
        @SystemMessage("""
            You are a mythology expert specializing in zodiac lore.
            Share an interesting myth, legend, or cultural story about the zodiac sign.
            Include Greek mythology references and cultural significance.
            Keep it engaging and educational (2-3 paragraphs).
            """)
        @UserMessage("Tell me a mythology story about the zodiac sign {{sign}}:")
        @Agent("Find mythology and stories related to a zodiac sign")
        String findStory(@V("sign") String sign);
    }

    /**
     * WriterAgent: Composes the final writeup from all gathered information.
     * Inputs: horoscope, story -> Output: writeup (the goal)
     */
    interface WriterAgent {
        @SystemMessage("""
            You are a skilled writer who creates beautiful, personalized astrology writeups.
            Combine the horoscope and mythology into a cohesive, engaging narrative.
            
            Structure your response as:
            1. A warm greeting
            2. Zodiac sign overview
            3. Today's horoscope predictions
            4. Mythological connections and stories
            5. A closing blessing
            
            Make it feel personal and magical.
            """)
        @UserMessage("""
            Create a personalized astrology writeup:
            
            Horoscope:
            {{horoscope}}
            
            Mythology:
            {{story}}
            """)
        @Agent("Compose personalized astrology writeup from horoscope and mythology")
        String write(@V("horoscope") String horoscope, @V("story") String story);
    }
}
