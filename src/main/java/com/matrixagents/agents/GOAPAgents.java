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
 * Dependency Graph (TSP Travel Planner):
 *   prompt -> cities (via CityParser)
 *   cities -> distances (via DistanceCalculator)  ─┐ parallel
 *   cities -> attractions (via AttractionFinder)  ─┘ branches
 *   distances -> route (via RouteOptimizer)
 *   route, attractions -> itinerary (via ItineraryPlanner) ← GOAL
 */
public interface GOAPAgents {

    /**
     * CityParser: Extracts the list of cities from the user's prompt.
     * Input: prompt -> Output: cities
     */
    interface CityParser {
        @SystemMessage("""
            You are a travel planning assistant. Extract the list of cities from the user's request.
            Return ONLY a comma-separated list of city names (e.g., "Paris, London, Rome, Berlin").
            If no specific cities are mentioned, suggest 4-5 interesting cities based on the context.
            """)
        @UserMessage("Extract the cities to visit from: {{prompt}}")
        @Agent("Parse and extract cities from the user's travel request")
        String parseCities(@V("prompt") String prompt);
    }

    /**
     * DistanceCalculator: Estimates distances between all city pairs.
     * Input: cities -> Output: distances
     */
    interface DistanceCalculator {
        @SystemMessage("""
            You are a geography expert. Given a list of cities, estimate the approximate
            driving/flying distances between ALL pairs of cities.
            Format as a compact distance matrix, e.g.:
            Paris→London: 450km, Paris→Rome: 1100km, London→Rome: 1430km
            Keep it concise and factual.
            """)
        @UserMessage("Calculate distances between these cities: {{cities}}")
        @Agent("Calculate distances between all city pairs for route optimization")
        String calculateDistances(@V("cities") String cities);
    }

    /**
     * AttractionFinder: Finds top attractions for each city.
     * Input: cities -> Output: attractions
     * NOTE: This runs in PARALLEL with DistanceCalculator (both depend only on cities)
     */
    interface AttractionFinder {
        @SystemMessage("""
            You are a travel expert. For each city, list 2-3 must-see attractions
            with a one-line description each.
            Keep it concise and practical for trip planning.
            """)
        @UserMessage("Find top attractions for each of these cities: {{cities}}")
        @Agent("Find top tourist attractions for each city")
        String findAttractions(@V("cities") String cities);
    }

    /**
     * RouteOptimizer: Finds the optimal travel route (TSP solution).
     * Input: distances -> Output: route
     */
    interface RouteOptimizer {
        @SystemMessage("""
            You are a route optimization expert solving the Travelling Salesman Problem.
            Given the distances between cities, find the shortest route that visits
            all cities exactly once and returns to the starting city.
            
            Show your reasoning briefly, then present the optimal route as:
            ROUTE: City1 → City2 → City3 → ... → City1
            TOTAL DISTANCE: approximately Xkm
            """)
        @UserMessage("Find the optimal route given these distances:\n{{distances}}")
        @Agent("Optimize travel route using TSP algorithm on distance data")
        String optimizeRoute(@V("distances") String distances);
    }

    /**
     * ItineraryPlanner: Creates the final travel itinerary.
     * Inputs: route, attractions -> Output: itinerary (the goal)
     * This agent CONVERGES the two parallel branches.
     */
    interface ItineraryPlanner {
        @SystemMessage("""
            You are a professional travel planner. Create a day-by-day travel itinerary
            that follows the optimized route and includes the best attractions at each stop.
            
            Structure your response as:
            - Day 1: [City] - Attractions to visit, travel tips
            - Day 2: [City] - Attractions to visit, travel tips
            - ...
            
            Include estimated travel times between cities and practical tips.
            Make it feel like a real, actionable travel plan.
            """)
        @UserMessage("""
            Create a travel itinerary combining the optimal route and attractions:
            
            Optimized Route:
            {{route}}
            
            Attractions:
            {{attractions}}
            """)
        @Agent("Create final travel itinerary from optimized route and attraction data")
        String planItinerary(@V("route") String route, @V("attractions") String attractions);
    }
}
