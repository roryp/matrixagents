package com.matrixagents.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * Agents for the LOOP PATTERN using langchain4j-agentic module.
 * Demonstrates iterative refinement with exit conditions.
 * Pattern: Generate story -> Score style -> Refine (loop until score >= 0.8).
 * 
 * Uses @Agent annotation with loopBuilder() for iterative workflows.
 * The StyleScorer writes to "score" which is checked by exit condition.
 */
public interface LoopAgents {

    /**
     * CreativeWriter: Creates an initial story.
     * Output key: "story" - initial story for loop
     */
    interface CreativeWriter {
        @UserMessage("""
            You are a creative writer.
            Generate a draft of a story no more than 3 sentences long around the given topic.
            Return only the story and nothing else.
            The topic is {{topic}}.
            """)
        @Agent(description = "Generates a story based on the given topic", outputKey = "story")
        String generateStory(@V("topic") String topic);
    }

    /**
     * StyleScorer: Evaluates how well the story matches the target style.
     * Returns a score between 0.0 and 1.0.
     * Output key: "score" - used for loop exit condition
     */
    interface StyleScorer {
        @UserMessage("""
            You are a critical reviewer.
            Give a review score between 0.0 and 1.0 for the following
            story based on how well it aligns with the style '{{style}}'.
            Return only the score and nothing else.
            
            The story is: "{{story}}"
            """)
        @Agent(description = "Scores a story based on how well it aligns with a given style", outputKey = "score")
        double scoreStyle(@V("story") String story, @V("style") String style);
    }

    /**
     * StyleEditor: Improves the story to better match the target style.
     * Output key: "story" - overwrites with refined version
     */
    interface StyleEditor {
        @UserMessage("""
            You are a professional editor.
            Analyze and rewrite the following story to better fit and be more coherent with the {{style}} style.
            Return only the story and nothing else.
            The story is "{{story}}".
            """)
        @Agent(description = "Edits a story to better fit a given style", outputKey = "story")
        String editStory(@V("story") String story, @V("style") String style);
    }

    /**
     * StyledWriter: Typed interface for the loop workflow.
     * Combines CreativeWriter with loop(StyleScorer -> StyleEditor)
     * Exit condition: score >= 0.8 or maxIterations(5)
     */
    interface StyledWriter {
        @Agent
        String writeStoryWithStyle(@V("topic") String topic, @V("style") String style);
    }
}
