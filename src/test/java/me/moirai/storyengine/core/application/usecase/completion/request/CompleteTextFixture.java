package me.moirai.storyengine.core.application.usecase.completion.request;

import me.moirai.storyengine.core.port.inbound.completion.CompleteText;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;

import java.util.ArrayList;
import java.util.List;

public class CompleteTextFixture {

    public static CompleteText.Message.Builder userMessage() {

        return CompleteText.Message.builder()
                .isAuthorBot(false)
                .messageContent("Message");
    }
    public static CompleteText.Message.Builder botMessage() {

        return CompleteText.Message.builder()
                .isAuthorBot(true)
                .messageContent("Message");
    }

    public static CompleteText.Builder withModerationDisabled() {

        List<CompleteText.Message> messages = new ArrayList<>();
        messages.add(userMessage().build());

        return CompleteText.builder()
                .aiModel("GPT4_OMNI")
                .requesterId("123456")
                .frequencyPenalty(0D)
                .logitBias(emptyMap())
                .maxTokenLimit(250)
                .messages(messages)
                .moderationLevel("disabled")
                .personaId("1234")
                .presencePenalty(0D)
                .stopSequences(emptySet())
                .temperature(1D)
                .worldId("12345");
    }

    public static CompleteText.Builder withStrictModeration() {

        List<CompleteText.Message> messages = new ArrayList<>();
        messages.add(userMessage().build());

        return CompleteText.builder()
                .aiModel("GPT4_OMNI")
                .requesterId("123456")
                .frequencyPenalty(0D)
                .logitBias(emptyMap())
                .maxTokenLimit(250)
                .messages(messages)
                .moderationLevel("strict")
                .personaId("1234")
                .presencePenalty(0D)
                .stopSequences(emptySet())
                .temperature(1D)
                .worldId("12345");
    }

    public static CompleteText.Builder withPermissiveModeration() {

        List<CompleteText.Message> messages = new ArrayList<>();
        messages.add(userMessage().build());

        return CompleteText.builder()
                .aiModel("GPT4_OMNI")
                .requesterId("123456")
                .frequencyPenalty(0D)
                .logitBias(emptyMap())
                .maxTokenLimit(250)
                .messages(messages)
                .moderationLevel("permissive")
                .personaId("1234")
                .presencePenalty(0D)
                .stopSequences(emptySet())
                .temperature(1D)
                .worldId("12345");
    }
}
