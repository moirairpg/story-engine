package me.moirai.storyengine.common.dto;

import me.moirai.storyengine.common.enums.AiRole;

public record ChatMessage(
        AiRole role,
        String content) {

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
