package me.moirai.storyengine.core.port.outbound.generation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import me.moirai.storyengine.common.enums.AiRole;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ChatMessage(
        @JsonProperty("role") AiRole role,
        @JsonProperty("content") String content) {

    public static ChatMessage asSystem(String content) {

        return new ChatMessage(AiRole.SYSTEM, content);
    }

    public static ChatMessage asAssistant(String content) {

        return new ChatMessage(AiRole.ASSISTANT, content);
    }

    public static ChatMessage asUser(String content) {

        return new ChatMessage(AiRole.USER, content);
    }
}
