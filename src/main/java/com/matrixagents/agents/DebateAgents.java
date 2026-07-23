package com.matrixagents.agents;

import static dev.langchain4j.agentic.patterns.debate.DebatePlanner.DEBATE_CONTEXT_KEY;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * Agents for the debate pattern.
 */
public interface DebateAgents {

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