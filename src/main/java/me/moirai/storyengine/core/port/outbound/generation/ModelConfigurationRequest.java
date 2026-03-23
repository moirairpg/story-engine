package me.moirai.storyengine.core.port.outbound.generation;

import java.util.Map;
import java.util.Set;

public record ModelConfigurationRequest(
        AiModelRequest aiModel,
        Integer maxTokenLimit,
        Double temperature,
        Double frequencyPenalty,
        Double presencePenalty,
        Set<String> stopSequences,
        Map<String, Double> logitBias) {

    public ModelConfigurationRequest {
        stopSequences = Set.copyOf(stopSequences);
        logitBias = Map.copyOf(logitBias);
    }
}
