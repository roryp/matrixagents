package com.matrixagents.agents;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * Agents for the SEQUENCE PATTERN.
 * Demonstrates chaining where each agent's output feeds into the next.
 * Pattern: CreativeWriter -> AudienceEditor -> StyleEditor
 */
public interface SequenceAgents {

    /**
     * CreativeWriter: Generates an initial story based on a topic.
     */
    interface CreativeWriter {
        @SystemMessage("""
            You are a creative fiction writer. Generate an engaging story based on the given topic.
            Focus on compelling characters, vivid settings, and an interesting plot.
            Keep the story to 3-4 paragraphs.
            """)
        @UserMessage("Write a story about: {{topic}}")
        String writeStory(@V("topic") String topic);
    }

    /**
     * AudienceEditor: Adapts the story for a specific target audience.
     */
    interface AudienceEditor {
        @SystemMessage("""
            You are an expert editor who adapts stories for specific audiences.
            Adjust vocabulary, themes, and complexity to match the target audience.
            Maintain the core story while making it more appropriate and engaging for the audience.
            """)
        @UserMessage("""
            Adapt this story for {{audience}} audience:
            
            {{story}}
            """)
        String editForAudience(@V("story") String story, @V("audience") String audience);
    }

    /**
     * StyleEditor: Applies a specific writing style to the story.
     */
    interface StyleEditor {
        @SystemMessage("""
            You are a style editor who transforms stories to match specific genres or styles.
            Apply the requested style while preserving the plot and characters.
            Add genre-appropriate elements, tone, and narrative techniques.
            """)
        @UserMessage("""
            Transform this story to {{style}} style:
            
            {{story}}
            """)
        String editForStyle(@V("story") String story, @V("style") String style);
    }
}
