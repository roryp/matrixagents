package com.matrixagents.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openaiofficial.OpenAiOfficialChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class LangChainConfig {

    @Value("${AZURE_OPENAI_API_KEY:}")
    private String apiKey;

    @Value("${AZURE_OPENAI_ENDPOINT:}")
    private String endpoint;

    @Value("${AZURE_OPENAI_DEPLOYMENT:gpt-5}")
    private String deploymentName;

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
