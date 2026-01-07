package com.matrixagents.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openaiofficial.OpenAiOfficialChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class LangChainConfig {

    @Value("${azure.openai.api-key:}")
    private String apiKey;

    @Value("${azure.openai.endpoint:}")
    private String endpoint;

    @Value("${azure.openai.deployment:gpt-5-nano}")
    private String deploymentName;

    @Value("${azure.openai.embedding-deployment:text-embedding-3-small}")
    private String embeddingDeploymentName;

    @Bean
    @Primary
    public ChatModel chatModel() {
        return OpenAiOfficialChatModel.builder()
                .baseUrl(endpoint)
                .apiKey(apiKey)
                .modelName(deploymentName)
                .isAzure(true)
                // GPT-5 only supports temperature=1.0
                .build();
    }

    @Bean("plannerModel")
    public ChatModel plannerModel() {
        return OpenAiOfficialChatModel.builder()
                .baseUrl(endpoint)
                .apiKey(apiKey)
                .modelName(deploymentName)
                .isAzure(true)
                // GPT-5 only supports temperature=1.0
                .build();
    }
}
