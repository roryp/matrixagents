package com.matrixagents.agents;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * Agents for the SEQUENCE PATTERN using langchain4j-agentic module.
 * Demonstrates chaining where each agent's output feeds into the next.
 * Pattern: CreativeWriter -> AudienceEditor -> StyleEditor
 * 
 * Uses @Agent annotation and AgenticScope for proper agent orchestration.
 */
public interface SequenceAgents {

    /**
     * CreativeWriter: Generates an initial story based on a topic.
     * Output key: "story" - feeds into subsequent agents
     */
    interface CreativeWriter {
        @UserMessage("""
            You are a creative fiction writer. Generate an engaging story based on the given topic.
            Focus on compelling characters, vivid settings, and an interesting plot.
            Keep the story to 3-4 paragraphs.
            The topic is {{topic}}.
            """)
        @Agent(description = "Generates a story based on the given topic", outputKey = "story")
        String generateStory(@V("topic") String topic);
    }

    /**
     * AudienceEditor: Adapts the story for a specific target audience.
     * Input: "story" from CreativeWriter, "audience" from scope
     * Output key: "story" - overwrites with adapted version
     */
    interface AudienceEditor {
        @UserMessage("""
            You are a professional editor.
            Analyze and rewrite the following story to better align with the target audience of {{audience}}.
            Adjust vocabulary, themes, and complexity to match the target audience.
            Return only the story and nothing else.
            The story is "{{story}}".
            """)
        @Agent(description = "Edits a story to better fit a given audience", outputKey = "story")
        String editForAudience(@V("story") String story, @V("audience") String audience);
    }

    /**
     * StyleEditor: Applies a specific writing style to the story.
     * Input: "story" from AudienceEditor, "style" from scope
     * Output key: "story" - final styled version
     */
    interface StyleEditor {
        @UserMessage("""
            You are a professional editor.
            Analyze and rewrite the following story to better fit and be more coherent with the {{style}} style.
            Apply genre-appropriate elements, tone, and narrative techniques.
            Return only the story and nothing else.
            The story is "{{story}}".
            """)
        @Agent(description = "Edits a story to better fit a given style", outputKey = "story")
        String editForStyle(@V("story") String story, @V("style") String style);
    }

    /**
     * NovelCreator: Typed interface for the complete sequential workflow.
     * Combines CreativeWriter -> AudienceEditor -> StyleEditor
     */
    interface NovelCreator {
        @Agent
        String createNovel(@V("topic") String topic, @V("audience") String audience, @V("style") String style);
    }
}
