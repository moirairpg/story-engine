package me.moirai.storyengine.core.application.command.message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.enums.AiRole;
import me.moirai.storyengine.common.exception.BusinessRuleViolationException;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.common.util.DefaultStringProcessors;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.message.Message;
import me.moirai.storyengine.core.port.inbound.message.MessageResult;
import me.moirai.storyengine.core.port.inbound.message.SendMessage;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureLorebookReader;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.generation.ChatMessage;
import me.moirai.storyengine.core.port.outbound.generation.EmbeddingPort;
import me.moirai.storyengine.core.port.outbound.generation.TextCompletionPort;
import me.moirai.storyengine.core.port.outbound.generation.TextGenerationRequest;
import me.moirai.storyengine.core.port.outbound.message.MessageData;
import me.moirai.storyengine.core.port.outbound.message.MessageReader;
import me.moirai.storyengine.core.port.outbound.message.MessageRepository;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;
import me.moirai.storyengine.core.port.outbound.vectorsearch.VectorSearchPort;

@CommandHandler
public class SendMessageHandler extends AbstractCommandHandler<SendMessage, MessageResult> {

    private final AdventureRepository adventureRepository;
    private final PersonaRepository personaRepository;
    private final MessageRepository messageRepository;
    private final MessageReader messageReader;
    private final TextCompletionPort textCompletionPort;
    private final EmbeddingPort embeddingPort;
    private final VectorSearchPort vectorSearchPort;
    private final AdventureLorebookReader lorebookReader;
    private final int messageWindowSize;
    private final int lorebookTopK;

    public SendMessageHandler(
            AdventureRepository adventureRepository,
            PersonaRepository personaRepository,
            MessageRepository messageRepository,
            MessageReader messageReader,
            TextCompletionPort textCompletionPort,
            EmbeddingPort embeddingPort,
            VectorSearchPort vectorSearchPort,
            AdventureLorebookReader lorebookReader,
            @Value("${moirai.adventure.message-window-size}") int messageWindowSize,
            @Value("${moirai.rag.lorebook.top-k}") int lorebookTopK) {

        this.adventureRepository = adventureRepository;
        this.personaRepository = personaRepository;
        this.messageRepository = messageRepository;
        this.messageReader = messageReader;
        this.textCompletionPort = textCompletionPort;
        this.embeddingPort = embeddingPort;
        this.vectorSearchPort = vectorSearchPort;
        this.lorebookReader = lorebookReader;
        this.messageWindowSize = messageWindowSize;
        this.lorebookTopK = lorebookTopK;
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
                .orElseThrow(() -> new NotFoundException("Adventure not found"));

        var persona = personaRepository.findById(adventure.getPersonaId())
                .orElseThrow(() -> new NotFoundException("Persona not found"));

        var playerMessage = Message.builder()
                .adventureId(adventure.getId())
                .role(AiRole.USER)
                .content(command.content())
                .build();

        messageRepository.save(playerMessage);

        var history = messageReader.findActiveByAdventureId(adventure.getId(), messageWindowSize);
        var context = assembleContext(adventure, history, command.content());
        var modelConfig = adventure.getModelConfiguration();

        var generationRequest = new TextGenerationRequest(
                modelConfig.getAiModel().getOfficialModelName(),
                persona.getPersonality(),
                context,
                modelConfig.getMaxTokenLimit(),
                modelConfig.getTemperature());

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
            Adventure adventure,
            List<MessageData> history,
            String currentMessage) {

        var context = new ArrayList<ChatMessage>();
        var contextAttributes = adventure.getContextAttributes();

        var lorebookMessages = retrieveLorebookContext(adventure.getPublicId(), currentMessage);
        context.addAll(lorebookMessages);

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

    private List<ChatMessage> retrieveLorebookContext(UUID adventurePublicId, String currentMessage) {

        var queryVector = embeddingPort.embed(currentMessage);
        var entryIds = vectorSearchPort.search(adventurePublicId, queryVector, lorebookTopK);

        if (entryIds.isEmpty()) {
            return List.of();
        }

        var entries = lorebookReader.getAllByIds(entryIds);

        return entries.stream()
                .map(e -> ChatMessage.asSystem(e.name() + ": " + e.description()))
                .toList();
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
