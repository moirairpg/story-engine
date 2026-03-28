package me.moirai.storyengine.core.port.inbound.adventure;

import java.util.Map;
import java.util.Set;

import me.moirai.storyengine.common.enums.ArtificialIntelligenceModel;

public record UpdateModelConfigurationDto(
        ArtificialIntelligenceModel aiModel,
        Integer maxTokenLimit,
        Double temperature,
        Double frequencyPenalty,
        Double presencePenalty,
        Set<String> stopSequencesToAdd,
        Set<String> stopSequencesToRemove,
        Map<String, Double> logitBiasToAdd,
        Set<String> logitBiasToRemove) {
}
