package me.moirai.discordbot.infrastructure.inbound.rest.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class TextCompletionRequestFixture {

    public static TextCompletionRequest.Message userMessage() {

        TextCompletionRequest.Message message = new TextCompletionRequest.Message();

        message.setIsAuthorBot(false);
        message.setMessageContent("Message");

        return message;
    }

    public static TextCompletionRequest withDisabledModeration() {

        List<TextCompletionRequest.Message> messages = new ArrayList<>();
        messages.add(userMessage());

        TextCompletionRequest request = new TextCompletionRequest();

        request.setAiModel("gpt35-turbo");
        request.setFrequencyPenalty(1D);
        request.setLogitBias(new HashMap<>());
        request.setMaxTokenLimit(250);
        request.setMessages(messages);
        request.setModerationLevel("disabled");
        request.setPersonaId("1234");
        request.setPresencePenalty(1D);
        request.setStopSequences(new HashSet<>());
        request.setTemperature(0.8);
        request.setWorldId("1234");

        return request;
    }

    public static TextCompletionRequest withStrictModeration() {

        List<TextCompletionRequest.Message> messages = new ArrayList<>();
        messages.add(userMessage());

        TextCompletionRequest request = new TextCompletionRequest();

        request.setAiModel("gpt35-turbo");
        request.setFrequencyPenalty(1D);
        request.setLogitBias(new HashMap<>());
        request.setMaxTokenLimit(250);
        request.setMessages(messages);
        request.setModerationLevel("strict");
        request.setPersonaId("1234");
        request.setPresencePenalty(1D);
        request.setStopSequences(new HashSet<>());
        request.setTemperature(0.8);
        request.setWorldId("1234");

        return request;
    }

    public static TextCompletionRequest withPermissiveModeration() {

        List<TextCompletionRequest.Message> messages = new ArrayList<>();
        messages.add(userMessage());

        TextCompletionRequest request = new TextCompletionRequest();

        request.setAiModel("gpt35-turbo");
        request.setFrequencyPenalty(1D);
        request.setLogitBias(new HashMap<>());
        request.setMaxTokenLimit(250);
        request.setMessages(messages);
        request.setModerationLevel("permissive");
        request.setPersonaId("1234");
        request.setPresencePenalty(1D);
        request.setStopSequences(new HashSet<>());
        request.setTemperature(0.8);
        request.setWorldId("1234");

        return request;
    }
}
