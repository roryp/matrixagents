package com.matrixagents.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * Agents for the P2P (Peer-to-Peer) PATTERN using langchain4j-agentic module.
 * 
 * P2P uses P2PPlanner which automatically:
 * 1. Activates agents when their required inputs become available in shared state
 * 2. Uses state-based triggering (reactive) rather than predetermined sequence
 * 3. Continues until an exit condition is met (e.g., score threshold)
 * 
 * Key: Each agent's @V parameters define when they can activate (inputs available).
 */
public interface P2PAgents {

    /**
     * LiteratureAgent: Searches and summarizes relevant research.
     * Input: topic -> Output: researchFindings
     */
    interface LiteratureAgent {
        @SystemMessage("""
            You are a research literature specialist. Search and summarize 
            the current state of knowledge on the given topic.
            
            Include:
            - Key findings from research
            - Major theories and frameworks
            - Gaps in current knowledge
            - Recent developments
            
            Be thorough but concise (3-4 paragraphs).
            """)
        @UserMessage("Research the current state of knowledge on: {{topic}}")
        @Agent("Research and summarize literature on a topic")
        String research(@V("topic") String topic);
    }

    /**
     * HypothesisAgent: Formulates hypotheses based on research.
     * Input: researchFindings -> Output: hypothesis
     */
    interface HypothesisAgent {
        @SystemMessage("""
            You are a hypothesis formulation expert. Based on the research findings,
            formulate a clear, testable hypothesis.
            
            Your hypothesis should:
            - Address a gap in knowledge
            - Be specific and measurable
            - Be logically derived from the research
            
            Format:
            HYPOTHESIS: [Clear statement]
            RATIONALE: [Why this hypothesis]
            PREDICTIONS: [What we'd expect if true]
            """)
        @UserMessage("""
            Based on this research, formulate a hypothesis:
            
            {{researchFindings}}
            """)
        @Agent("Formulate hypothesis based on research findings")
        String formulate(@V("researchFindings") String researchFindings);
    }

    /**
     * CriticAgent: Critiques hypotheses and identifies weaknesses.
     * Input: hypothesis -> Output: critique
     */
    interface CriticAgent {
        @SystemMessage("""
            You are a scientific critic. Your role is to identify weaknesses,
            potential flaws, and areas for improvement in hypotheses.
            
            Be constructive but thorough:
            - Logical consistency
            - Testability issues
            - Alternative explanations
            - Potential confounds
            
            Format:
            STRENGTHS: [What's good]
            WEAKNESSES: [Problems identified]
            SUGGESTIONS: [How to improve]
            """)
        @UserMessage("""
            Critique this hypothesis:
            
            {{hypothesis}}
            """)
        @Agent("Critique hypothesis and identify weaknesses")
        String critique(@V("hypothesis") String hypothesis);
    }

    /**
     * ValidationAgent: Validates or reformulates based on critique.
     * Inputs: hypothesis, critique -> Output: hypothesis (refined)
     */
    interface ValidationAgent {
        @SystemMessage("""
            You are a validation specialist. Review the hypothesis and critique,
            then either validate the hypothesis or reformulate it to address weaknesses.
            
            If the hypothesis is sound despite critique: explain why and validate it.
            If changes are needed: reformulate to address the critique.
            
            Format:
            STATUS: [VALIDATED / REFORMULATED]
            HYPOTHESIS: [Final version]
            CHANGES: [What was changed and why, if any]
            """)
        @UserMessage("""
            Review and validate or reformulate:
            
            ORIGINAL HYPOTHESIS:
            {{hypothesis}}
            
            CRITIQUE:
            {{critique}}
            """)
        @Agent("Validate or reformulate hypothesis based on critique")
        String validate(@V("hypothesis") String hypothesis, @V("critique") String critique);
    }

    /**
     * ScorerAgent: Scores the quality of the hypothesis.
     * Input: hypothesis -> Output: score (Double)
     */
    interface ScorerAgent {
        @SystemMessage("""
            You are a hypothesis quality scorer. Evaluate the hypothesis on these criteria:
            
            - Clarity (0-0.2): Is it clearly stated?
            - Testability (0-0.2): Can it be empirically tested?
            - Novelty (0-0.2): Does it offer new insights?
            - Logical soundness (0-0.2): Is the reasoning valid?
            - Impact potential (0-0.2): If true, would it be significant?
            
            IMPORTANT: Return ONLY a number between 0.0 and 1.0 representing the total score.
            Example: 0.75
            """)
        @UserMessage("Score this hypothesis (return only a number 0.0-1.0): {{hypothesis}}")
        @Agent("Score the quality of a hypothesis")
        Double score(@V("hypothesis") String hypothesis);
    }

    /**
     * Typed interface for the P2P workflow.
     */
    interface ResearchWorkflow {
        @Agent
        String conductResearch(@V("topic") String topic);
    }
}
