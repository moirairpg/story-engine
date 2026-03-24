package me.moirai.storyengine.core.port.inbound.adventure;

import java.util.Map;
import java.util.Set;

import me.moirai.storyengine.common.enums.ArtificialIntelligenceModel;

public record ModelConfigurationDto(
        ArtificialIntelligenceModel aiModel,
        int maxTokenLimit,
        double temperature,
        double frequencyPenalty,
        double presencePenalty,
        Set<String> stopSequences,
        Map<String, Double> logitBias) {

    public ModelConfigurationDto {
        stopSequences = Set.copyOf(stopSequences);
        logitBias = Map.copyOf(logitBias);
    }
}
