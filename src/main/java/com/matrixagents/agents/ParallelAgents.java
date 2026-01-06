package com.matrixagents.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.declarative.Output;
import dev.langchain4j.agentic.declarative.ParallelAgent;
import dev.langchain4j.agentic.declarative.ParallelExecutor;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Agents for the PARALLEL PATTERN using langchain4j-agentic module.
 * Demonstrates concurrent agent execution with result combination.
 * Pattern: FoodExpert + MovieExpert run in parallel, results combined.
 * 
 * Uses @Agent annotation with @ParallelAgent for concurrent execution.
 */
public interface ParallelAgents {

    /**
     * FoodExpert: Suggests meal options based on mood/occasion.
     * Output key: "meals" - stored in AgenticScope
     */
    interface FoodExpert {
        @UserMessage("""
            You are a great evening planner.
            Propose a list of 3 meals matching the given mood.
            The mood is {{mood}}.
            For each meal, just give the name of the meal.
            Provide a list with the 3 items and nothing else.
            """)
        @Agent(description = "Suggests meals based on the given mood", outputKey = "meals")
        List<String> findMeal(@V("mood") String mood);
    }

    /**
     * MovieExpert: Recommends movies based on mood/occasion.
     * Output key: "movies" - stored in AgenticScope
     */
    interface MovieExpert {
        @UserMessage("""
            You are a great evening planner.
            Propose a list of 3 movies matching the given mood.
            The mood is {{mood}}.
            Provide a list with the 3 items and nothing else.
            """)
        @Agent(description = "Recommends movies based on the given mood", outputKey = "movies")
        List<String> findMovie(@V("mood") String mood);
    }

    /**
     * Record to hold a combined evening plan.
     */
    record EveningPlan(String movie, String meal) {}

    /**
     * EveningPlannerAgent: Typed interface for the parallel workflow.
     * Combines FoodExpert + MovieExpert running in parallel.
     * Uses @Output to combine results into EveningPlan list.
     */
    interface EveningPlannerAgent {
        @ParallelAgent(outputKey = "plans", subAgents = {FoodExpert.class, MovieExpert.class})
        List<EveningPlan> plan(@V("mood") String mood);

        @ParallelExecutor
        static Executor executor() {
            return Executors.newFixedThreadPool(2);
        }

        @Output
        static List<EveningPlan> createPlans(@V("movies") List<String> movies, @V("meals") List<String> meals) {
            List<EveningPlan> plans = new ArrayList<>();
            for (int i = 0; i < movies.size(); i++) {
                if (i >= meals.size()) break;
                plans.add(new EveningPlan(movies.get(i), meals.get(i)));
            }
            return plans;
        }
    }
}
