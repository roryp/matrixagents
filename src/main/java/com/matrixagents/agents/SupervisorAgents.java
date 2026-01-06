package com.matrixagents.agents;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.service.SystemMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Agents and Tools for the SUPERVISOR PATTERN.
 * Demonstrates autonomous agent orchestration with tool integration.
 * Pattern: Supervisor coordinates WithdrawAgent, CreditAgent, ExchangeAgent.
 * Each agent has access to specific tools (@Tool).
 */
public interface SupervisorAgents {

    /**
     * Bank account tool providing banking operations.
     */
    class BankTool {
        private final Map<String, Double> accounts = new ConcurrentHashMap<>(Map.of(
                "checking", 1000.0,
                "savings", 5000.0
        ));

        @Tool("Withdraw money from an account. Returns the new balance or an error message.")
        public String withdraw(
                @P("Account name (checking or savings)") String account,
                @P("Amount to withdraw") double amount) {
            String key = account.toLowerCase();
            if (!accounts.containsKey(key)) {
                return "Error: Account '" + account + "' not found";
            }
            double balance = accounts.get(key);
            if (amount > balance) {
                return "Error: Insufficient funds. Balance: $" + balance;
            }
            accounts.put(key, balance - amount);
            return "Withdrew $" + amount + " from " + account + ". New balance: $" + accounts.get(key);
        }

        @Tool("Credit/deposit money to an account. Returns the new balance.")
        public String credit(
                @P("Account name (checking or savings)") String account,
                @P("Amount to credit") double amount) {
            String key = account.toLowerCase();
            if (!accounts.containsKey(key)) {
                return "Error: Account '" + account + "' not found";
            }
            accounts.put(key, accounts.get(key) + amount);
            return "Credited $" + amount + " to " + account + ". New balance: $" + accounts.get(key);
        }

        @Tool("Get the current balance of an account.")
        public String getBalance(@P("Account name (checking or savings)") String account) {
            String key = account.toLowerCase();
            if (!accounts.containsKey(key)) {
                return "Error: Account '" + account + "' not found";
            }
            return account + " balance: $" + accounts.get(key);
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
                "USD_JPY", 149.50,
                "EUR_USD", 1.09,
                "GBP_USD", 1.27
        );

        @Tool("Exchange currency at current rates. Returns the converted amount.")
        public String exchange(
                @P("Source currency (USD, EUR, GBP, JPY)") String from,
                @P("Target currency (USD, EUR, GBP, JPY)") String to,
                @P("Amount to exchange") double amount) {
            String key = from.toUpperCase() + "_" + to.toUpperCase();
            if (!RATES.containsKey(key)) {
                return "Error: Exchange rate for " + from + " to " + to + " not available";
            }
            double rate = RATES.get(key);
            double converted = amount * rate;
            return String.format("Exchanged %.2f %s to %.2f %s (rate: %.4f)",
                    amount, from.toUpperCase(), converted, to.toUpperCase(), rate);
        }

        @Tool("Get the current exchange rate between two currencies.")
        public String getRate(
                @P("Source currency") String from,
                @P("Target currency") String to) {
            String key = from.toUpperCase() + "_" + to.toUpperCase();
            if (!RATES.containsKey(key)) {
                return "Rate not available for " + from + " to " + to;
            }
            return String.format("1 %s = %.4f %s", from.toUpperCase(), RATES.get(key), to.toUpperCase());
        }
    }

    /**
     * WithdrawAgent: Handles withdrawal requests using BankTool.
     */
    interface WithdrawAgent {
        @SystemMessage("""
            You are a bank withdrawal specialist. Your job is to process withdrawal requests.
            Use the withdraw tool to process the request.
            Always confirm the account and amount before processing.
            Report the result including the new balance.
            """)
        String processWithdrawal(String request);
    }

    /**
     * CreditAgent: Handles deposit/credit requests using BankTool.
     */
    interface CreditAgent {
        @SystemMessage("""
            You are a bank deposit specialist. Your job is to process deposits and credits.
            Use the credit tool to process the request.
            Always confirm the account and amount before processing.
            Report the result including the new balance.
            """)
        String processCredit(String request);
    }

    /**
     * ExchangeAgent: Handles currency exchange using ExchangeTool.
     */
    interface ExchangeAgent {
        @SystemMessage("""
            You are a currency exchange specialist. Your job is to convert currencies.
            Use the exchange tool to process conversions.
            Always show the exchange rate used.
            """)
        String processExchange(String request);
    }

    /**
     * BankSupervisor: Orchestrates the sub-agents based on the request.
     */
    interface BankSupervisor {
        @SystemMessage("""
            You are a bank supervisor AI that orchestrates banking operations.
            Analyze the customer's request and determine which operation is needed:
            
            - WITHDRAW: For withdrawal requests
            - CREDIT: For deposit/credit requests
            - EXCHANGE: For currency conversion requests
            - BALANCE: For balance inquiries
            
            First respond with the operation type, then provide detailed instructions
            for the appropriate sub-agent.
            
            Format:
            OPERATION: [type]
            DETAILS: [specific instructions]
            """)
        String analyze(String request);
    }
}
