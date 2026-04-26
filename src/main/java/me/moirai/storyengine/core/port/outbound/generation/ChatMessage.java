package me.moirai.storyengine.core.port.outbound.generation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import me.moirai.storyengine.common.enums.MessageAuthorRole;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ChatMessage(
        @JsonProperty("role") MessageAuthorRole role,
        @JsonProperty("content") String content) {

    public static ChatMessage asSystem(String content) {

        return new ChatMessage(MessageAuthorRole.SYSTEM, content);
    }

    public static ChatMessage asAssistant(String content) {

        return new ChatMessage(MessageAuthorRole.ASSISTANT, content);
    }

    public static ChatMessage asUser(String content) {

        return new ChatMessage(MessageAuthorRole.USER, content);
    }
}
