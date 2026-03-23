package me.moirai.storyengine.infrastructure.outbound.adapter.request;

import java.util.Map;
import java.util.Set;

import me.moirai.storyengine.common.enums.ArtificialIntelligenceModel;
import me.moirai.storyengine.core.port.outbound.generation.AiModelRequest;
import me.moirai.storyengine.core.port.outbound.generation.ModelConfigurationRequest;

public class ModelConfigurationRequestFixture {

    public static ModelConfigurationRequest gpt4Mini() {

        var aiModel = ArtificialIntelligenceModel.GPT4_MINI;
        return new ModelConfigurationRequest(
                new AiModelRequest(aiModel.toString(), aiModel.getOfficialModelName(),
                        aiModel.getHardTokenLimit()),
                100, 1.0, 0.2, 0.2,
                Set.of("ABC"),
                Map.of("ABC", 50.0, "DEF", 5.0));
    }

    public static ModelConfigurationRequest gpt4Omni() {

        var aiModel = ArtificialIntelligenceModel.GPT4_OMNI;
        return new ModelConfigurationRequest(
                new AiModelRequest(aiModel.toString(), aiModel.getOfficialModelName(),
                        aiModel.getHardTokenLimit()),
                100, 1.0, 0.2, 0.2,
                Set.of("ABC"),
                Map.of("ABC", 50.0, "DEF", 5.0));
    }
}
