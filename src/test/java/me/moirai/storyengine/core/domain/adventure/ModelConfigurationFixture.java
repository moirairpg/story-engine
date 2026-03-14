package me.moirai.storyengine.core.domain.adventure;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ModelConfigurationFixture {

    public static ModelConfiguration.Builder gpt4Mini() {

        ModelConfiguration.Builder modelConfigurationBuilder = ModelConfiguration.builder();
        modelConfigurationBuilder.aiModel(ArtificialIntelligenceModel.GPT4_MINI);
        modelConfigurationBuilder.frequencyPenalty(0.2);
        modelConfigurationBuilder.presencePenalty(0.2);
        modelConfigurationBuilder.maxTokenLimit(100);
        modelConfigurationBuilder.temperature(1.0);

        Map<String, Double> logitBias = new HashMap<>();
        logitBias.put("ABC", 50.0);
        logitBias.put("DEF", 5.0);

        Set<String> stopSequences = new HashSet<>();
        stopSequences.add("ABC");

        modelConfigurationBuilder.logitBias(logitBias);
        modelConfigurationBuilder.stopSequences(stopSequences);

        return modelConfigurationBuilder;
    }

    public static ModelConfiguration.Builder gpt4Omni() {

        ModelConfiguration.Builder modelConfigurationBuilder = ModelConfiguration.builder();
        modelConfigurationBuilder.aiModel(ArtificialIntelligenceModel.GPT4_OMNI);
        modelConfigurationBuilder.frequencyPenalty(0.2);
        modelConfigurationBuilder.presencePenalty(0.2);
        modelConfigurationBuilder.maxTokenLimit(100);
        modelConfigurationBuilder.temperature(1.0);

        Map<String, Double> logitBias = new HashMap<>();
        logitBias.put("ABC", 50.0);
        logitBias.put("DEF", 5.0);

        Set<String> stopSequences = new HashSet<>();
        stopSequences.add("ABC");

        modelConfigurationBuilder.logitBias(logitBias);
        modelConfigurationBuilder.stopSequences(stopSequences);

        return modelConfigurationBuilder;
    }
}
