package me.moirai.storyengine.core.domain.adventure;

import me.moirai.storyengine.common.enums.ArtificialIntelligenceModel;

public class ModelConfigurationFixture {

    public static ModelConfiguration gpt35Turbo() {

        return ModelConfiguration.builder()
                .aiModel(ArtificialIntelligenceModel.GPT54_NANO)
                .maxTokenLimit(100)
                .temperature(1.0)
                .build();
    }

    public static ModelConfiguration gpt4Mini() {

        return ModelConfiguration.builder()
                .aiModel(ArtificialIntelligenceModel.GPT54_MINI)
                .maxTokenLimit(100)
                .temperature(1.0)
                .build();
    }

    public static ModelConfiguration gpt4Omni() {

        return ModelConfiguration.builder()
                .aiModel(ArtificialIntelligenceModel.GPT54)
                .maxTokenLimit(100)
                .temperature(1.0)
                .build();
    }
}
