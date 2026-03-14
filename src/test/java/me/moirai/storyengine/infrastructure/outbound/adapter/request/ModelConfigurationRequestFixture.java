package me.moirai.storyengine.infrastructure.outbound.adapter.request;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import me.moirai.storyengine.core.domain.adventure.ArtificialIntelligenceModel;
import me.moirai.storyengine.core.port.outbound.generation.AiModelRequest;
import me.moirai.storyengine.core.port.outbound.generation.ModelConfigurationRequest;

public class ModelConfigurationRequestFixture {

    public static ModelConfigurationRequest.Builder gpt4Mini() {

        ModelConfigurationRequest.Builder modelConfigurationBuilder = ModelConfigurationRequest.builder();
        modelConfigurationBuilder.frequencyPenalty(0.2);
        modelConfigurationBuilder.presencePenalty(0.2);
        modelConfigurationBuilder.maxTokenLimit(100);
        modelConfigurationBuilder.temperature(1.0);

        ArtificialIntelligenceModel aiModel = ArtificialIntelligenceModel.GPT4_MINI;
        modelConfigurationBuilder.aiModel(AiModelRequest.build(aiModel.toString(),
                aiModel.getOfficialModelName(),
                aiModel.getHardTokenLimit()));

        Map<String, Double> logitBias = new HashMap<>();
        logitBias.put("ABC", 50.0);
        logitBias.put("DEF", 5.0);

        Set<String> stopSequences = new HashSet<>();
        stopSequences.add("ABC");

        modelConfigurationBuilder.logitBias(logitBias);
        modelConfigurationBuilder.stopSequences(stopSequences);

        return modelConfigurationBuilder;
    }

    public static ModelConfigurationRequest.Builder gpt4Omni() {

        ModelConfigurationRequest.Builder modelConfigurationBuilder = ModelConfigurationRequest.builder();
        modelConfigurationBuilder.frequencyPenalty(0.2);
        modelConfigurationBuilder.presencePenalty(0.2);
        modelConfigurationBuilder.maxTokenLimit(100);
        modelConfigurationBuilder.temperature(1.0);

        ArtificialIntelligenceModel aiModel = ArtificialIntelligenceModel.GPT4_OMNI;
        modelConfigurationBuilder.aiModel(AiModelRequest.build(aiModel.toString(),
                aiModel.getOfficialModelName(),
                aiModel.getHardTokenLimit()));

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
