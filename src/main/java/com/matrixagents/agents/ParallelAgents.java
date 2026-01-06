package com.matrixagents.agents;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

import java.util.List;

/**
 * Agents for the PARALLEL PATTERN.
 * Demonstrates concurrent agent execution with result combination.
 * Pattern: FoodExpert + MovieExpert run in parallel, results combined.
 */
public interface ParallelAgents {

    /**
     * FoodExpert: Suggests meal options based on mood/occasion.
     */
    interface FoodExpert {
        @SystemMessage("""
            You are a culinary expert who suggests perfect meal pairings.
            Consider the mood, occasion, and atmosphere when making suggestions.
            Return exactly 3 meal suggestions, one per line.
            Format: Just the meal name/description, no numbering.
            """)
        @UserMessage("Suggest 3 meals perfect for a {{mood}} evening:")
        String suggestMeals(@V("mood") String mood);
    }

    /**
     * MovieExpert: Recommends movies based on mood/occasion.
     */
    interface MovieExpert {
        @SystemMessage("""
            You are a film expert who recommends movies for different moods.
            Consider the emotional tone and occasion when making suggestions.
            Return exactly 3 movie suggestions, one per line.
            Format: "Movie Title (Year)" - no descriptions.
            """)
        @UserMessage("Recommend 3 movies perfect for a {{mood}} evening:")
        String recommendMovies(@V("mood") String mood);
    }

    /**
     * PlanCombiner: Synthesizes the parallel results into evening plans.
     */
    interface PlanCombiner {
        @SystemMessage("""
            You are an evening planner who creates cohesive plans.
            Combine movie and meal suggestions into 3 complete evening plans.
            Each plan should pair one movie with one meal that complement each other.
            Format each as: "🎬 [Movie] + 🍽️ [Meal]: [Brief reason they pair well]"
            """)
        @UserMessage("""
            Create 3 evening plans from these options:
            
            Movies:
            {{movies}}
            
            Meals:
            {{meals}}
            """)
        String combinePlans(@V("movies") String movies, @V("meals") String meals);
    }

    /**
     * Record to hold a combined evening plan.
     */
    record EveningPlan(String movie, String meal, String reason) {}
}
