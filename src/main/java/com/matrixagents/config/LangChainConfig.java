package com.matrixagents.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openaiofficial.OpenAiOfficialChatModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class LangChainConfig {

    @ConfigProperty(name = "azure.openai.api-key")
    String apiKey;

    @ConfigProperty(name = "azure.openai.endpoint")
    String endpoint;

    @ConfigProperty(name = "azure.openai.deployment", defaultValue = "gpt-5-nano")
    String deploymentName;

    @ConfigProperty(name = "azure.openai.embedding-deployment", defaultValue = "text-embedding-3-small")
    String embeddingDeploymentName;

    @Produces
    @ApplicationScoped
    @Named("defaultChatModel")
    public ChatModel chatModel() {
        return OpenAiOfficialChatModel.builder()
                .baseUrl(endpoint)
                .apiKey(apiKey)
                .modelName(deploymentName)
                .isAzure(true)
                .build();
    }

    @Produces
    @ApplicationScoped
    @Named("plannerModel")
    public ChatModel plannerModel() {
        return OpenAiOfficialChatModel.builder()
                .baseUrl(endpoint)
                .apiKey(apiKey)
                .modelName(deploymentName)
                .isAzure(true)
                .build();
    }
}
