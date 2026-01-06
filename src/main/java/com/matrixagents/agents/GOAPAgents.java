package com.matrixagents.agents;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * Agents for the GOAP (Goal-Oriented Action Planning) PATTERN.
 * Demonstrates planning where agents are selected based on available state to achieve a goal.
 * Pattern: Planner analyzes goal -> Determines required agents -> Executes plan.
 */
public interface GOAPAgents {

    /**
     * Record types for GOAP state.
     */
    record Person(String name, String birthDate) {}
    record ZodiacSign(String sign, String element, String ruling_planet) {}

    /**
     * GoalPlanner: Analyzes the goal and determines the execution plan.
     */
    interface GoalPlanner {
        @SystemMessage("""
            You are a goal-oriented action planner. Given a goal and current state,
            determine the sequence of actions needed to achieve the goal.
            
            Available agents and what they need/produce:
            - PersonExtractor: needs (prompt) -> produces (person name, birth date)
            - SignExtractor: needs (birth date) -> produces (zodiac sign)
            - HoroscopeGenerator: needs (sign) -> produces (horoscope)
            - StoryFinder: needs (sign) -> produces (related mythology/stories)
            - WriterAgent: needs (horoscope, stories) -> produces (final writeup)
            
            Analyze what information is available and what agents are needed.
            
            Format your response as:
            PLAN:
            1. [Agent] - [Reason]
            2. [Agent] - [Reason]
            ...
            MISSING: [What info is missing, if any]
            """)
        @UserMessage("""
            Goal: {{goal}}
            
            Current state:
            {{state}}
            """)
        String createPlan(@V("goal") String goal, @V("state") String state);
    }

    /**
     * PersonExtractor: Extracts person information from the prompt.
     */
    interface PersonExtractor {
        @SystemMessage("""
            You are an entity extractor. Extract person information from the text.
            Look for names and birth dates (or any date that could be a birthday).
            
            Format response as:
            NAME: [extracted name or "unknown"]
            BIRTHDATE: [extracted date in YYYY-MM-DD format or "unknown"]
            """)
        @UserMessage("Extract person info from: {{prompt}}")
        String extractPerson(@V("prompt") String prompt);
    }

    /**
     * SignExtractor: Determines zodiac sign from birth date.
     */
    interface SignExtractor {
        @SystemMessage("""
            You are a zodiac expert. Given a birth date, determine the zodiac sign.
            Provide the sign with its element and ruling planet.
            
            Format:
            SIGN: [zodiac sign]
            ELEMENT: [Fire/Earth/Air/Water]
            PLANET: [ruling planet]
            """)
        @UserMessage("What zodiac sign is someone born on {{birthDate}}?")
        String determineSign(@V("birthDate") String birthDate);
    }

    /**
     * HoroscopeGenerator: Creates a horoscope for a zodiac sign.
     */
    interface HoroscopeGenerator {
        @SystemMessage("""
            You are an astrologer. Generate a detailed, personalized horoscope.
            Include predictions for love, career, health, and general fortune.
            Make it engaging and positive while feeling authentic.
            """)
        @UserMessage("Generate a horoscope for {{sign}} ({{element}} sign, ruled by {{planet}}):")
        String generateHoroscope(@V("sign") String sign, @V("element") String element, @V("planet") String planet);
    }

    /**
     * StoryFinder: Finds mythology and stories related to the zodiac sign.
     */
    interface StoryFinder {
        @SystemMessage("""
            You are a mythology expert specializing in zodiac lore.
            Share interesting myths, legends, and cultural stories about the zodiac sign.
            Include Greek mythology, cultural significance, and famous personalities.
            Keep it engaging and educational.
            """)
        @UserMessage("Tell me the mythology and stories behind {{sign}}:")
        String findStories(@V("sign") String sign);
    }

    /**
     * WriterAgent: Composes the final writeup from all gathered information.
     */
    interface WriterAgent {
        @SystemMessage("""
            You are a skilled writer who creates beautiful, personalized astrology writeups.
            Combine the horoscope and mythology into a cohesive, engaging narrative.
            If a person's name is known, personalize the writeup.
            
            Structure:
            1. Personalized greeting (if name known)
            2. Zodiac overview
            3. Today's horoscope
            4. Mythological connections
            5. Closing blessing
            """)
        @UserMessage("""
            Create a personalized astrology writeup:
            
            Person: {{personName}}
            Sign: {{sign}}
            
            Horoscope:
            {{horoscope}}
            
            Mythology:
            {{mythology}}
            """)
        String compose(@V("personName") String personName, @V("sign") String sign,
                       @V("horoscope") String horoscope, @V("mythology") String mythology);
    }
}
