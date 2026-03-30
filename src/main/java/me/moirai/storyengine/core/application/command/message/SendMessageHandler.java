package me.moirai.storyengine.core.application.command.message;

import static me.moirai.storyengine.common.util.DefaultStringProcessors.addChatPrefix;
import static me.moirai.storyengine.common.util.DefaultStringProcessors.formatScene;
import static me.moirai.storyengine.common.util.DefaultStringProcessors.replacePersonaNamePlaceholderWith;
import static me.moirai.storyengine.common.util.DefaultStringProcessors.stripAsNamePrefix;
import static me.moirai.storyengine.common.util.DefaultStringProcessors.stripAsNamePrefixForLowercase;
import static me.moirai.storyengine.common.util.DefaultStringProcessors.stripChatPrefix;
import static me.moirai.storyengine.common.util.DefaultStringProcessors.stripTrailingFragment;

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
import me.moirai.storyengine.common.security.authentication.MoiraiSecurityContext;
import me.moirai.storyengine.common.util.StringProcessor;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.message.Message;
import me.moirai.storyengine.core.port.inbound.message.MessageResult;
import me.moirai.storyengine.core.port.inbound.message.SendMessage;
import org.springframework.context.ApplicationEventPublisher;

import me.moirai.storyengine.core.domain.chronicle.MessageWindowOverflowEvent;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureLorebookReader;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.chronicle.ChronicleSegmentReader;
import me.moirai.storyengine.core.port.outbound.generation.ChatMessage;
import me.moirai.storyengine.core.port.outbound.generation.EmbeddingPort;
import me.moirai.storyengine.core.port.outbound.generation.TextCompletionPort;
import me.moirai.storyengine.core.port.outbound.generation.TextGenerationRequest;
import me.moirai.storyengine.core.port.outbound.message.MessageData;
import me.moirai.storyengine.core.port.outbound.message.MessageReader;
import me.moirai.storyengine.core.port.outbound.message.MessageRepository;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;
import me.moirai.storyengine.core.port.outbound.vectorsearch.ChronicleVectorSearchPort;
import me.moirai.storyengine.core.port.outbound.vectorsearch.LorebookVectorSearchPort;

@CommandHandler
public class SendMessageHandler extends AbstractCommandHandler<SendMessage, MessageResult> {

    private final AdventureRepository adventureRepository;
    private final PersonaRepository personaRepository;
    private final MessageRepository messageRepository;
    private final MessageReader messageReader;
    private final TextCompletionPort textCompletionPort;
    private final EmbeddingPort embeddingPort;
    private final LorebookVectorSearchPort vectorSearchPort;
    private final AdventureLorebookReader lorebookReader;
    private final ChronicleVectorSearchPort chronicleVectorSearchPort;
    private final ChronicleSegmentReader chronicleSegmentReader;
    private final ApplicationEventPublisher eventPublisher;
    private final int messageWindowSize;
    private final int lorebookTopK;
    private final int chronicleTopK;

    public SendMessageHandler(
            AdventureRepository adventureRepository,
            PersonaRepository personaRepository,
            MessageRepository messageRepository,
            MessageReader messageReader,
            TextCompletionPort textCompletionPort,
            EmbeddingPort embeddingPort,
            LorebookVectorSearchPort vectorSearchPort,
            AdventureLorebookReader lorebookReader,
            ChronicleVectorSearchPort chronicleVectorSearchPort,
            ChronicleSegmentReader chronicleSegmentReader,
            ApplicationEventPublisher eventPublisher,
            @Value("${moirai.adventure.message-window-size}") int messageWindowSize,
            @Value("${moirai.rag.lorebook.top-k}") int lorebookTopK,
            @Value("${moirai.rag.chronicle.top-k}") int chronicleTopK) {

        this.adventureRepository = adventureRepository;
        this.personaRepository = personaRepository;
        this.messageRepository = messageRepository;
        this.messageReader = messageReader;
        this.textCompletionPort = textCompletionPort;
        this.embeddingPort = embeddingPort;
        this.vectorSearchPort = vectorSearchPort;
        this.lorebookReader = lorebookReader;
        this.chronicleVectorSearchPort = chronicleVectorSearchPort;
        this.chronicleSegmentReader = chronicleSegmentReader;
        this.eventPublisher = eventPublisher;
        this.messageWindowSize = messageWindowSize;
        this.lorebookTopK = lorebookTopK;
        this.chronicleTopK = chronicleTopK;
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

        var username = MoiraiSecurityContext.getAuthenticatedUser().username();

        var characterName = adventure.getLorebookEntryByPlayerId(username)
                .map(e -> e.getName())
                .orElse(username);

        var playerMessage = Message.builder()
                .adventureId(adventure.getId())
                .role(AiRole.USER)
                .content(addChatPrefix(characterName).apply(command.content()))
                .build();

        messageRepository.save(playerMessage);

        var history = messageReader.findActiveByAdventureId(adventure.getId(), messageWindowSize);

        var personality = replacePersonaNamePlaceholderWith(persona.getName())
                .apply(persona.getPersonality());

        var context = assembleContext(adventure, history, command.content());
        var modelConfig = adventure.getModelConfiguration();

        var generationRequest = new TextGenerationRequest(
                modelConfig.getAiModel().getOfficialModelName(),
                personality,
                context,
                modelConfig.getMaxTokenLimit(),
                modelConfig.getTemperature());

        var generationResult = textCompletionPort.generateTextFrom(generationRequest);

        var responseProcessor = new StringProcessor();
        responseProcessor.addRule(stripChatPrefix());
        responseProcessor.addRule(stripAsNamePrefix(persona.getName()));
        responseProcessor.addRule(stripAsNamePrefixForLowercase(persona.getName()));
        responseProcessor.addRule(stripTrailingFragment());

        var cleanedResponse = responseProcessor.process(generationResult.getOutputText());

        var aiMessage = Message.builder()
                .adventureId(adventure.getId())
                .role(AiRole.ASSISTANT)
                .content(addChatPrefix(persona.getName()).apply(cleanedResponse))
                .build();

        messageRepository.save(aiMessage);

        eventPublisher.publishEvent(new MessageWindowOverflowEvent(adventure.getPublicId()));

        return new MessageResult(
                aiMessage.getPublicId(),
                cleanedResponse,
                aiMessage.getRole(),
                aiMessage.getCreationDate());
    }

    private List<ChatMessage> assembleContext(
            Adventure adventure,
            List<MessageData> history,
            String currentMessage) {

        var context = new ArrayList<ChatMessage>();
        var contextAttributes = adventure.getContextAttributes();

        var queryVector = embeddingPort.embed(currentMessage);

        context.addAll(retrieveLorebookContext(adventure.getPublicId(), queryVector));
        context.addAll(retrieveChronicleContext(adventure.getPublicId(), queryVector));

        context.addAll(interleaveBumps(
                history.stream().map(this::toChatMessage).toList(),
                contextAttributes.bump(),
                contextAttributes.bumpFrequency()));

        if (contextAttributes.authorsNote() != null && !contextAttributes.authorsNote().isBlank()) {
            context.add(ChatMessage.asSystem(contextAttributes.authorsNote()));
        }

        if (contextAttributes.scene() != null && !contextAttributes.scene().isBlank()) {
            context.add(ChatMessage.asSystem(
                    formatScene().apply(contextAttributes.scene())));
        }

        if (contextAttributes.nudge() != null && !contextAttributes.nudge().isBlank()) {
            context.add(ChatMessage.asSystem(contextAttributes.nudge()));
        }

        return Collections.unmodifiableList(context);
    }

    private List<ChatMessage> retrieveLorebookContext(UUID adventurePublicId, float[] queryVector) {

        var entryIds = vectorSearchPort.search(adventurePublicId, queryVector, lorebookTopK);

        if (entryIds.isEmpty()) {
            return List.of();
        }

        var entries = lorebookReader.getAllByIds(entryIds);

        return entries.stream()
                .map(e -> ChatMessage.asSystem(e.name() + ": " + e.description()))
                .toList();
    }

    private List<ChatMessage> retrieveChronicleContext(UUID adventurePublicId, float[] queryVector) {

        var segmentIds = chronicleVectorSearchPort.search(adventurePublicId, queryVector, chronicleTopK);

        if (segmentIds.isEmpty()) {
            return List.of();
        }

        var segments = chronicleSegmentReader.getAllByIds(segmentIds);

        return segments.stream()
                .map(s -> ChatMessage.asSystem(s.content()))
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
