package me.moirai.storyengine.core.application.command.message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.enums.AiRole;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.exception.BusinessRuleViolationException;
import me.moirai.storyengine.common.util.DefaultStringProcessors;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.message.Message;
import me.moirai.storyengine.core.domain.persona.Persona;
import me.moirai.storyengine.core.port.inbound.message.MessageResult;
import me.moirai.storyengine.core.port.inbound.message.SendMessage;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.generation.ChatMessage;
import me.moirai.storyengine.core.port.outbound.generation.TextCompletionPort;
import me.moirai.storyengine.core.port.outbound.generation.TextGenerationRequest;
import me.moirai.storyengine.core.port.outbound.message.MessageData;
import me.moirai.storyengine.core.port.outbound.message.MessageReader;
import me.moirai.storyengine.core.port.outbound.message.MessageRepository;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;

@CommandHandler
public class SendMessageHandler extends AbstractCommandHandler<SendMessage, MessageResult> {

    private final AdventureRepository adventureRepository;
    private final PersonaRepository personaRepository;
    private final MessageRepository messageRepository;
    private final MessageReader messageReader;
    private final TextCompletionPort textCompletionPort;
    private final int messageWindowSize;

    public SendMessageHandler(
            AdventureRepository adventureRepository,
            PersonaRepository personaRepository,
            MessageRepository messageRepository,
            MessageReader messageReader,
            TextCompletionPort textCompletionPort,
            @Value("${moirai.adventure.message-window-size}") int messageWindowSize) {

        this.adventureRepository = adventureRepository;
        this.personaRepository = personaRepository;
        this.messageRepository = messageRepository;
        this.messageReader = messageReader;
        this.textCompletionPort = textCompletionPort;
        this.messageWindowSize = messageWindowSize;
    }

    @Override
    public void validate(SendMessage command) {

        if (command.adventureId() == null) {
            throw new IllegalArgumentException("Adventure ID cannot be null");
        }

        if (command.content() == null || command.content().isBlank()) {
            throw new IllegalArgumentException("Message content cannot be blank");
        }
    }

    @Override
    public MessageResult execute(SendMessage command) {

        var adventure = adventureRepository.findByPublicId(command.adventureId())
                .orElseThrow(() -> new AssetNotFoundException("Adventure not found"));

        var persona = personaRepository.findById(adventure.getPersonaId())
                .orElseThrow(() -> new AssetNotFoundException("Persona not found"));

        var playerMessage = Message.builder()
                .adventureId(adventure.getId())
                .role(AiRole.USER)
                .content(command.content())
                .build();

        messageRepository.save(playerMessage);

        var history = messageReader.findActiveByAdventureId(adventure.getId(), messageWindowSize);
        var context = assembleContext(persona, adventure, history);
        var modelConfig = adventure.getModelConfiguration();

        var generationRequest = new TextGenerationRequest(
                modelConfig.getAiModel().getOfficialModelName(),
                context,
                modelConfig.getStopSequences(),
                modelConfig.getMaxTokenLimit(),
                modelConfig.getTemperature(),
                modelConfig.getPresencePenalty(),
                modelConfig.getFrequencyPenalty(),
                modelConfig.getLogitBias());

        var generationResult = textCompletionPort.generateTextFrom(generationRequest);

        var aiMessage = Message.builder()
                .adventureId(adventure.getId())
                .role(AiRole.ASSISTANT)
                .content(generationResult.getOutputText())
                .build();

        messageRepository.save(aiMessage);

        return new MessageResult(
                aiMessage.getPublicId(),
                aiMessage.getContent(),
                aiMessage.getRole(),
                aiMessage.getCreationDate());
    }

    private List<ChatMessage> assembleContext(
            Persona persona,
            Adventure adventure,
            List<MessageData> history) {

        var context = new ArrayList<ChatMessage>();
        var contextAttributes = adventure.getContextAttributes();

        context.add(ChatMessage.asSystem(persona.getPersonality()));

        context.addAll(interleaveBumps(
                history.stream().map(this::toChatMessage).toList(),
                contextAttributes.bump(),
                contextAttributes.bumpFrequency()));

        if (contextAttributes.authorsNote() != null && !contextAttributes.authorsNote().isBlank()) {
            context.add(ChatMessage.asSystem(contextAttributes.authorsNote()));
        }

        if (contextAttributes.scene() != null && !contextAttributes.scene().isBlank()) {
            context.add(ChatMessage.asSystem(
                    DefaultStringProcessors.formatScene().apply(contextAttributes.scene())));
        }

        if (contextAttributes.nudge() != null && !contextAttributes.nudge().isBlank()) {
            context.add(ChatMessage.asSystem(contextAttributes.nudge()));
        }

        return Collections.unmodifiableList(context);
    }

    private List<ChatMessage> interleaveBumps(
            List<ChatMessage> messages,
            String bump,
            int bumpFrequency) {

        if (bump == null || bump.isBlank() || bumpFrequency <= 0) {
            return messages;
        }

        var result = new ArrayList<ChatMessage>();
        var size = messages.size();

        for (var i = 0; i < size; i++) {
            result.add(messages.get(i));

            if ((size - i) % bumpFrequency == 0) {
                result.add(ChatMessage.asSystem(bump));
            }
        }

        return Collections.unmodifiableList(result);
    }

    private ChatMessage toChatMessage(MessageData message) {
        return switch (message.role()) {
            case USER -> ChatMessage.asUser(message.content());
            case ASSISTANT -> ChatMessage.asAssistant(message.content());
            default -> throw new BusinessRuleViolationException("Unexpected role in message history: " + message.role());
        };
    }
}
