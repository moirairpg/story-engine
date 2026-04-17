package me.moirai.storyengine.core.domain.message;

import me.moirai.storyengine.common.enums.AiRole;

public class MessageFixture {

    public static Message.Builder userMessage() {

        return Message.builder()
                .adventureId(1L)
                .role(AiRole.USER)
                .content("Hello, adventurer!");
    }

    public static Message.Builder assistantMessage() {

        return Message.builder()
                .adventureId(1L)
                .role(AiRole.ASSISTANT)
                .content("Greetings, brave hero!");
    }
}
