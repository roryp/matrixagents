package com.matrixagents.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * Agents for the P2P (Peer-to-Peer) PATTERN using langchain4j-agentic module.
 * Demonstrates collaborative agent network where agents communicate as peers.
 * Pattern: Agents activate when their required inputs become available in shared state.
 * 
 * Uses @Agent annotation with state-based activation for peer collaboration.
 */
public interface P2PAgents {

    /**
     * LiteratureAgent: Searches and summarizes relevant research.
     * Activates when: topic is available
     * Output key: "research"
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
            
            Be thorough but concise. Cite conceptual sources.
            """)
        @UserMessage("Research the current state of knowledge on: {{topic}}")
        @Agent(description = "Researches and summarizes literature on a topic", outputKey = "research")
        String research(@V("topic") String topic);
    }

    /**
     * HypothesisAgent: Formulates hypotheses based on research.
     * Activates when: research is available
     * Output key: "hypothesis"
     */
    interface HypothesisAgent {
        @SystemMessage("""
            You are a hypothesis formulation expert. Based on the research findings,
            formulate a clear, testable hypothesis.
            
            Your hypothesis should:
            - Address a gap in knowledge
            - Be specific and measurable
            - Be logically derived from the research
            - Include reasoning for why this hypothesis is promising
            
            Format:
            HYPOTHESIS: [Clear statement]
            RATIONALE: [Why this hypothesis]
            PREDICTIONS: [What we'd expect if true]
            """)
        @UserMessage("""
            Based on this research, formulate a hypothesis:
            
            {{research}}
            """)
        @Agent(description = "Formulates hypotheses based on research", outputKey = "hypothesis")
        String formulate(@V("research") String research);
    }

    /**
     * CriticAgent: Critiques hypotheses and identifies weaknesses.
     * Activates when: hypothesis is available
     * Output key: "critique"
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
            - Scope limitations
            
            Format:
            STRENGTHS: [What's good]
            WEAKNESSES: [Problems identified]
            SUGGESTIONS: [How to improve]
            """)
        @UserMessage("""
            Critique this hypothesis:
            
            {{hypothesis}}
            """)
        @Agent(description = "Critiques hypotheses and identifies weaknesses", outputKey = "critique")
        String critique(@V("hypothesis") String hypothesis);
    }

    /**
     * ValidationAgent: Validates or reformulates based on critique.
     * Activates when: hypothesis AND critique are available
     * Output key: "validation"
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
        @Agent(description = "Validates or reformulates hypotheses based on critique", outputKey = "validation")
        String validate(@V("hypothesis") String hypothesis, @V("critique") String critique);
    }

    /**
     * ScorerAgent: Scores the quality of the validated hypothesis.
     * Activates when: validation is available
     * Output key: "score"
     */
    interface ScorerAgent {
        @SystemMessage("""
            You are a hypothesis quality scorer. Evaluate the hypothesis on these criteria:
            
            - Clarity (0-0.2): Is it clearly stated?
            - Testability (0-0.2): Can it be empirically tested?
            - Novelty (0-0.2): Does it offer new insights?
            - Logical soundness (0-0.2): Is the reasoning valid?
            - Impact potential (0-0.2): If true, would it be significant?
            
            IMPORTANT: Start your response with "SCORE: X.XX" where X.XX is the total (0.0-1.0)
            Then provide the breakdown.
            """)
        @UserMessage("""
            Score this hypothesis:
            
            {{hypothesis}}
            """)
        @Agent(description = "Scores the quality of hypotheses", outputKey = "score")
        String score(@V("hypothesis") String hypothesis);
    }

    /**
     * SynthesizerAgent: Creates the final research output from all peer contributions.
     * Activates when: all outputs are available (research, hypothesis, critique, validation, score)
     * Output key: "report"
     */
    interface SynthesizerAgent {
        @SystemMessage("""
            You are a research synthesizer. Combine all the peer contributions into
            a cohesive research summary.
            
            Include:
            1. Executive Summary
            2. Background (from literature research)
            3. Proposed Hypothesis
            4. Critical Analysis
            5. Validation Results
            6. Quality Assessment
            7. Recommendations for Next Steps
            
            Make it professional and well-structured.
            """)
        @UserMessage("""
            Synthesize this peer research:
            
            LITERATURE RESEARCH:
            {{research}}
            
            HYPOTHESIS:
            {{hypothesis}}
            
            CRITIQUE:
            {{critique}}
            
            VALIDATION:
            {{validation}}
            
            SCORE: {{score}}
            """)
        @Agent(description = "Synthesizes research from all peer contributions", outputKey = "report")
        String synthesize(@V("research") String research, @V("hypothesis") String hypothesis,
                          @V("critique") String critique, @V("validation") String validation,
                          @V("score") String score);
    }

    /**
     * ResearchWorkflow: Typed interface for the P2P collaborative workflow.
     * Peers activate based on available state and collaborate towards research report.
     */
    interface ResearchWorkflow {
        @Agent
        String conductResearch(@V("topic") String topic);
    }
}
