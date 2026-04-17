package me.moirai.storyengine.infrastructure.outbound.adapter.request;

import me.moirai.storyengine.common.enums.ArtificialIntelligenceModel;
import me.moirai.storyengine.core.port.outbound.generation.AiModelRequest;
import me.moirai.storyengine.core.port.outbound.generation.ModelConfigurationRequest;

public class ModelConfigurationRequestFixture {

    public static ModelConfigurationRequest gpt4Mini() {

        var aiModel = ArtificialIntelligenceModel.GPT54_MINI;
        return new ModelConfigurationRequest(
                new AiModelRequest(aiModel.toString(), aiModel.getOfficialModelName(),
                        aiModel.getHardTokenLimit()),
                100, 1.0);
    }

    public static ModelConfigurationRequest gpt4Omni() {

        var aiModel = ArtificialIntelligenceModel.GPT54;
        return new ModelConfigurationRequest(
                new AiModelRequest(aiModel.toString(), aiModel.getOfficialModelName(),
                        aiModel.getHardTokenLimit()),
                100, 1.0);
    }
}
