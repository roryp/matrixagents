package com.matrixagents.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * Agents for the VOTING PATTERN using langchain4j-agentic-patterns 1.18 (VotingPlanner).
 *
 * An ensemble of independent "voter" agents all receive the SAME request and run in parallel.
 * Each returns a discrete vote, and a {@link dev.langchain4j.agentic.patterns.voting.VotingStrategy}
 * (here {@code majority()}) aggregates the votes into a single collective decision.
 *
 * Wiring: {@code plannerBuilder().subAgents(growth, value, risk)
 *           .outputKey("decision").planner(() -> new VotingPlanner(VotingStrategy.majority()))}.
 * Each voter is built with a distinct outputKey so the individual ballots are visible in the scope.
 *
 * For {@code majority()} to find a consensus the votes must be directly comparable, so every voter
 * is instructed to answer with EXACTLY ONE uppercase token: BUY, HOLD, or SELL.
 */
public interface VotingAgents {

    /**
     * GrowthAnalyst: votes from a growth / upside perspective. outputKey (build time): "voteGrowth".
     */
    interface GrowthAnalyst {
        @SystemMessage("""
            You are a growth-focused investment analyst on a committee. You care about market size,
            revenue momentum, and long-term upside.
            """)
        @UserMessage("""
            Cast your committee vote on the following investment opportunity from a GROWTH perspective.
            Answer with EXACTLY ONE uppercase word and nothing else: BUY, HOLD, or SELL.
            Opportunity: {{proposal}}
            """)
        @Agent(description = "Votes BUY/HOLD/SELL from a growth perspective", name = "GrowthAnalyst")
        String vote(@V("proposal") String proposal);
    }

    /**
     * ValueAnalyst: votes from a valuation / fundamentals perspective. outputKey (build time): "voteValue".
     */
    interface ValueAnalyst {
        @SystemMessage("""
            You are a value-focused investment analyst on a committee. You care about valuation,
            profitability, cash flow, and margin of safety.
            """)
        @UserMessage("""
            Cast your committee vote on the following investment opportunity from a VALUE perspective.
            Answer with EXACTLY ONE uppercase word and nothing else: BUY, HOLD, or SELL.
            Opportunity: {{proposal}}
            """)
        @Agent(description = "Votes BUY/HOLD/SELL from a valuation perspective", name = "ValueAnalyst")
        String vote(@V("proposal") String proposal);
    }

    /**
     * RiskAnalyst: votes from a risk-management perspective. outputKey (build time): "voteRisk".
     */
    interface RiskAnalyst {
        @SystemMessage("""
            You are a risk-management analyst on a committee. You care about downside, debt,
            volatility, and capital preservation.
            """)
        @UserMessage("""
            Cast your committee vote on the following investment opportunity from a RISK perspective.
            Answer with EXACTLY ONE uppercase word and nothing else: BUY, HOLD, or SELL.
            Opportunity: {{proposal}}
            """)
        @Agent(description = "Votes BUY/HOLD/SELL from a risk-management perspective", name = "RiskAnalyst")
        String vote(@V("proposal") String proposal);
    }
}
