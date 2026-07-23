package com.matrixagents.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

import static dev.langchain4j.agentic.patterns.debate.DebatePlanner.DEBATE_CONTEXT_KEY;

/**
 * Agents for the DEBATE PATTERN using langchain4j-agentic-patterns 1.18 (DebatePlanner).
 *
 * Debaters generate independent positions in parallel, then enter critique rounds where they can
 * read the full debate history (via the shared {@code debateContext} scope key) and refine their
 * arguments. Rounds continue until the debaters converge on the same verdict or {@code maxRounds}
 * is reached, at which point the JUDGE (the last registered sub-agent) renders a final verdict.
 *
 * Wiring: {@code plannerBuilder().subAgents(proponent, skeptic, pragmatist, judge)
 *           .outputKey("verdict").planner(() -> new DebatePlanner(3, ConvergenceStrategy.unanimousLastWord()))}.
 * Each debater must be built with a DISTINCT outputKey so they don't overwrite each other.
 */
public interface DebateAgents {

    /**
     * ProponentDebater: argues IN FAVOR of the proposition. outputKey (set at build time): "proponent".
     */
    interface ProponentDebater {
        @UserMessage("""
            You are an optimistic proponent in a structured debate.
            Argue IN FAVOR of the proposition, emphasizing benefits and opportunities.
            If previous debate context is provided, weigh the other debaters' arguments and refine your position.
            Keep your response to 2-3 sentences. End with a one-word verdict: AGREE or DISAGREE.
            Question: {{question}}
            Previous debate context: {{debateContext}}
            """)
        @Agent(description = "Argues in favor of the proposition", name = "Proponent")
        String debate(@V("question") String question, @V(DEBATE_CONTEXT_KEY) String debateContext);
    }

    /**
     * SkepticDebater: argues AGAINST the proposition. outputKey (set at build time): "skeptic".
     */
    interface SkepticDebater {
        @UserMessage("""
            You are a cautious skeptic in a structured debate.
            Argue AGAINST the proposition, emphasizing risks, costs, and unintended consequences.
            If previous debate context is provided, weigh the other debaters' arguments and refine your position.
            Keep your response to 2-3 sentences. End with a one-word verdict: AGREE or DISAGREE.
            Question: {{question}}
            Previous debate context: {{debateContext}}
            """)
        @Agent(description = "Argues against the proposition", name = "Skeptic")
        String debate(@V("question") String question, @V(DEBATE_CONTEXT_KEY) String debateContext);
    }

    /**
     * PragmatistDebater: argues from practical, real-world outcomes. outputKey (set at build time): "pragmatist".
     */
    interface PragmatistDebater {
        @UserMessage("""
            You are a pragmatist in a structured debate.
            Argue based on practical consequences, feasibility, and real-world trade-offs.
            If previous debate context is provided, weigh the other debaters' arguments and refine your position.
            Keep your response to 2-3 sentences. End with a one-word verdict: AGREE or DISAGREE.
            Question: {{question}}
            Previous debate context: {{debateContext}}
            """)
        @Agent(description = "Argues from a pragmatic, real-world perspective", name = "Pragmatist")
        String debate(@V("question") String question, @V(DEBATE_CONTEXT_KEY) String debateContext);
    }

    /**
     * DebateJudge: the LAST sub-agent. Synthesizes the whole debate into a final verdict.
     * outputKey (set at build time): "verdict". Only receives the debateContext (the question is
     * already embedded in the debate exchanges).
     */
    interface DebateJudge {
        @UserMessage("""
            You are an impartial debate moderator.
            Review the debate context where several debaters argued a question from different perspectives.
            Synthesize their arguments and deliver a balanced, well-reasoned final verdict in 3-4 sentences.
            Debate context: {{debateContext}}
            """)
        @Agent(description = "Renders a final verdict by synthesizing the debate arguments", name = "Judge")
        String judge(@V("debateContext") String debateContext);
    }
}
