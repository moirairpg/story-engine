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

        return new ModelConfiguration(
                ArtificialIntelligenceModel.GPT35_TURBO,
                100,
                1.0,
                0.2,
                0.2,
                stopSequences,
                logitBias);
    }

    public static ModelConfiguration gpt4Mini() {

        Map<String, Double> logitBias = new HashMap<>();
        logitBias.put("ABC", 50.0);
        logitBias.put("DEF", 5.0);

        Set<String> stopSequences = new HashSet<>();
        stopSequences.add("ABC");

        return new ModelConfiguration(
                ArtificialIntelligenceModel.GPT4_MINI,
                100,
                1.0,
                0.2,
                0.2,
                stopSequences,
                logitBias);
    }

    public static ModelConfiguration gpt4Omni() {

        Map<String, Double> logitBias = new HashMap<>();
        logitBias.put("ABC", 50.0);
        logitBias.put("DEF", 5.0);

        Set<String> stopSequences = new HashSet<>();
        stopSequences.add("ABC");

        return new ModelConfiguration(
                ArtificialIntelligenceModel.GPT4_OMNI,
                100,
                1.0,
                0.2,
                0.2,
                stopSequences,
                logitBias);
    }
}
