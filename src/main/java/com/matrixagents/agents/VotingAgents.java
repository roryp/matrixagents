package com.matrixagents.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * Agents for the voting pattern.
 */
public interface VotingAgents {

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