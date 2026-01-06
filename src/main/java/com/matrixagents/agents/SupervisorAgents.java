package com.matrixagents.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Agents and Tools for the SUPERVISOR PATTERN using langchain4j-agentic module.
 * Demonstrates autonomous agent orchestration with tool integration.
 * Pattern: Supervisor autonomously coordinates WithdrawAgent, CreditAgent, ExchangeAgent.
 * 
 * Uses supervisorBuilder() for LLM-based planning and coordination.
 * The supervisor generates a plan and invokes sub-agents based on the request.
 */
public interface SupervisorAgents {

    /**
     * Bank account tool providing banking operations.
     */
    class BankTool {
        private final Map<String, Double> accounts = new ConcurrentHashMap<>(Map.of(
                "mario", 1000.0,
                "georgios", 1000.0
        ));

        public void createAccount(String user, Double initialBalance) {
            if (accounts.containsKey(user.toLowerCase())) {
                throw new RuntimeException("Account for user " + user + " already exists");
            }
            accounts.put(user.toLowerCase(), initialBalance);
        }

        public double getBalance(String user) {
            Double balance = accounts.get(user.toLowerCase());
            if (balance == null) {
                throw new RuntimeException("No balance found for user " + user);
            }
            return balance;
        }

        @Tool("Credit the given user with the given amount and return the new balance")
        public Double credit(@P("user name") String user, @P("amount") Double amount) {
            Double balance = accounts.get(user.toLowerCase());
            if (balance == null) {
                throw new RuntimeException("No balance found for user " + user);
            }
            Double newBalance = balance + amount;
            accounts.put(user.toLowerCase(), newBalance);
            return newBalance;
        }

        @Tool("Withdraw the given amount from the given user and return the new balance")
        public Double withdraw(@P("user name") String user, @P("amount") Double amount) {
            Double balance = accounts.get(user.toLowerCase());
            if (balance == null) {
                throw new RuntimeException("No balance found for user " + user);
            }
            Double newBalance = balance - amount;
            accounts.put(user.toLowerCase(), newBalance);
            return newBalance;
        }

        public Map<String, Double> getAllBalances() {
            return new HashMap<>(accounts);
        }
    }

    /**
     * Currency exchange tool.
     */
    class ExchangeTool {
        private static final Map<String, Double> RATES = Map.of(
                "USD_EUR", 0.92,
                "USD_GBP", 0.79,
                "EUR_USD", 1.09,
                "GBP_USD", 1.27
        );

        @Tool("Exchange the given amount of money from the original to the target currency")
        public Double exchange(
                @P("originalCurrency") String originalCurrency,
                @P("amount") Double amount,
                @P("targetCurrency") String targetCurrency) {
            String key = originalCurrency.toUpperCase() + "_" + targetCurrency.toUpperCase();
            Double rate = RATES.get(key);
            if (rate == null) {
                throw new RuntimeException("Exchange rate for " + originalCurrency + " to " + targetCurrency + " not available");
            }
            return amount * rate;
        }
    }

    /**
     * WithdrawAgent: Handles withdrawal requests using BankTool.
     * Output key: not specified (supervisor manages)
     */
    interface WithdrawAgent {
        @SystemMessage("""
            You are a banker that can only withdraw US dollars (USD) from a user account.
            """)
        @UserMessage("""
            Withdraw {{amount}} USD from {{user}}'s account and return the new balance.
            """)
        @Agent(description = "A banker that withdraws USD from an account")
        String withdraw(@V("user") String user, @V("amount") Double amount);
    }

    /**
     * CreditAgent: Handles deposit/credit requests using BankTool.
     */
    interface CreditAgent {
        @SystemMessage("""
            You are a banker that can only credit US dollars (USD) to a user account.
            """)
        @UserMessage("""
            Credit {{amount}} USD to {{user}}'s account and return the new balance.
            """)
        @Agent(description = "A banker that credits USD to an account")
        String credit(@V("user") String user, @V("amount") Double amount);
    }

    /**
     * ExchangeAgent: Handles currency exchange using ExchangeTool.
     */
    interface ExchangeAgent {
        @UserMessage("""
            You are an operator exchanging money in different currencies.
            Use the tool to exchange {{amount}} {{originalCurrency}} into {{targetCurrency}}
            returning only the final amount provided by the tool as it is and nothing else.
            """)
        @Agent(description = "A money exchanger that converts a given amount of money from the original to the target currency")
        Double exchange(
                @V("originalCurrency") String originalCurrency,
                @V("amount") Double amount,
                @V("targetCurrency") String targetCurrency);
    }

    /**
     * SupervisorAgent: Typed interface for the supervisor pattern.
     * The supervisor autonomously plans and coordinates sub-agents.
     */
    interface BankSupervisor {
        @Agent
        String invoke(@V("request") String request);
    }
}
