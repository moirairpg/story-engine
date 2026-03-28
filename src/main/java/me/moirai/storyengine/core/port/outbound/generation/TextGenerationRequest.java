package me.moirai.storyengine.core.port.outbound.generation;

import java.util.List;

public record TextGenerationRequest(

        String model,
        String instructions,
        List<ChatMessage> messages,
        Integer maxTokens,
        Double temperature) {

    public TextGenerationRequest {
        messages = List.copyOf(messages);
    }
}
