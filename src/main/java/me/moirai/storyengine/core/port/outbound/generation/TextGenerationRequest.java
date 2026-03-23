package me.moirai.storyengine.core.port.outbound.generation;

import java.util.List;
import java.util.Map;
import java.util.Set;

import me.moirai.storyengine.common.dto.ChatMessage;

public record TextGenerationRequest(

        String model,
        List<ChatMessage> messages,
        Set<String> stopSequences,
        Integer maxTokens,
        Double temperature,
        Double presencePenalty,
        Double frequencyPenalty,
        Map<String, Double> logitBias) {

    public TextGenerationRequest {
        messages = List.copyOf(messages);
        stopSequences = Set.copyOf(stopSequences);
        logitBias = Map.copyOf(logitBias);
    }
}