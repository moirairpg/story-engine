package me.moirai.storyengine.core.domain.adventure;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import me.moirai.storyengine.common.enums.ArtificialIntelligenceModel;

public class ModelConfigurationFixture {

    public static ModelConfiguration gpt35Turbo() {

        Map<String, Double> logitBias = new HashMap<>();
        logitBias.put("ABC", 50.0);
        logitBias.put("DEF", 5.0);

        Set<String> stopSequences = new HashSet<>();
        stopSequences.add("ABC");

        return ModelConfiguration.builder()
                .aiModel(ArtificialIntelligenceModel.GPT54_NANO)
                .maxTokenLimit(100)
                .temperature(1.0)
                .frequencyPenalty(0.2)
                .presencePenalty(0.2)
                .stopSequences(stopSequences)
                .logitBias(logitBias)
                .build();
    }

    public static ModelConfiguration gpt4Mini() {

        Map<String, Double> logitBias = new HashMap<>();
        logitBias.put("ABC", 50.0);
        logitBias.put("DEF", 5.0);

        Set<String> stopSequences = new HashSet<>();
        stopSequences.add("ABC");

        return ModelConfiguration.builder()
                .aiModel(ArtificialIntelligenceModel.GPT54_MINI)
                .maxTokenLimit(100)
                .temperature(1.0)
                .frequencyPenalty(0.2)
                .presencePenalty(0.2)
                .stopSequences(stopSequences)
                .logitBias(logitBias)
                .build();
    }

    public static ModelConfiguration gpt4Omni() {

        Map<String, Double> logitBias = new HashMap<>();
        logitBias.put("ABC", 50.0);
        logitBias.put("DEF", 5.0);

        Set<String> stopSequences = new HashSet<>();
        stopSequences.add("ABC");

        return ModelConfiguration.builder()
                .aiModel(ArtificialIntelligenceModel.GPT54)
                .maxTokenLimit(100)
                .temperature(1.0)
                .frequencyPenalty(0.2)
                .presencePenalty(0.2)
                .stopSequences(stopSequences)
                .logitBias(logitBias)
                .build();
    }
}
