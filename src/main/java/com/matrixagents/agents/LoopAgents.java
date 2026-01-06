package com.matrixagents.agents;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * Agents for the LOOP PATTERN.
 * Demonstrates iterative refinement with exit conditions.
 * Pattern: Generate -> Score -> Refine (loop until score threshold met).
 */
public interface LoopAgents {

    /**
     * StoryGenerator: Creates an initial story with a target style.
     */
    interface StoryGenerator {
        @SystemMessage("""
            You are a creative writer who generates stories in specific styles.
            Create an engaging story that attempts to match the requested style.
            Keep the story to 2-3 paragraphs.
            """)
        @UserMessage("Write a {{style}} story about: {{topic}}")
        String generate(@V("topic") String topic, @V("style") String style);
    }

    /**
     * StyleScorer: Evaluates how well the story matches the target style.
     * Returns a score between 0.0 and 1.0.
     */
    interface StyleScorer {
        @SystemMessage("""
            You are a literary critic who evaluates writing style alignment.
            Score how well the story matches the target style on a scale of 0.0 to 1.0.
            
            Consider:
            - Vocabulary appropriate to the style
            - Tone and atmosphere
            - Genre conventions
            - Narrative techniques
            
            IMPORTANT: Your response must start with "SCORE: X.XX" on the first line,
            where X.XX is a number between 0.0 and 1.0.
            Then provide a brief explanation of what could be improved.
            """)
        @UserMessage("""
            Evaluate how well this story matches the "{{style}}" style:
            
            {{story}}
            """)
        String score(@V("story") String story, @V("style") String style);
    }

    /**
     * StyleRefiner: Improves the story to better match the target style.
     */
    interface StyleRefiner {
        @SystemMessage("""
            You are an expert editor who refines stories to better match target styles.
            Take the feedback and improve the story to better align with the style.
            Maintain the core plot while enhancing style elements.
            Return ONLY the improved story, no explanations.
            """)
        @UserMessage("""
            Improve this story to better match the "{{style}}" style.
            
            Current story:
            {{story}}
            
            Feedback to address:
            {{feedback}}
            """)
        String refine(@V("story") String story, @V("style") String style, @V("feedback") String feedback);
    }
}
