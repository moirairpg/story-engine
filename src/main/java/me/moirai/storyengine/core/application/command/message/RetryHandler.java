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
import org.springframework.context.ApplicationEventPublisher;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.enums.AiRole;
import me.moirai.storyengine.common.exception.BusinessRuleViolationException;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.common.util.StringProcessor;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.chronicle.MessageWindowOverflowEvent;
import me.moirai.storyengine.core.domain.message.Message;
import me.moirai.storyengine.core.port.inbound.message.MessageResult;
import me.moirai.storyengine.core.port.inbound.message.Retry;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.chronicle.ChronicleSegmentRepository;
import me.moirai.storyengine.core.port.outbound.generation.ChatMessage;
import me.moirai.storyengine.core.port.outbound.generation.EmbeddingPort;
import me.moirai.storyengine.core.port.outbound.generation.TextCompletionPort;
import me.moirai.storyengine.core.port.outbound.generation.TextGenerationRequest;
import me.moirai.storyengine.core.port.outbound.message.MessageRepository;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;
import me.moirai.storyengine.core.port.outbound.vectorsearch.ChronicleVectorSearchPort;
import me.moirai.storyengine.core.port.outbound.vectorsearch.LorebookVectorSearchPort;

@CommandHandler
public class RetryHandler extends AbstractCommandHandler<Retry, MessageResult> {

    private final AdventureRepository adventureRepository;
    private final PersonaRepository personaRepository;
    private final MessageRepository messageRepository;
    private final TextCompletionPort textCompletionPort;
    private final EmbeddingPort embeddingPort;
    private final LorebookVectorSearchPort vectorSearchPort;
    private final ChronicleVectorSearchPort chronicleVectorSearchPort;
    private final ChronicleSegmentRepository chronicleSegmentRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final int messageWindowSize;
    private final int lorebookTopK;
    private final int chronicleTopK;

    public RetryHandler(
            AdventureRepository adventureRepository,
            PersonaRepository personaRepository,
            MessageRepository messageRepository,
            TextCompletionPort textCompletionPort,
            EmbeddingPort embeddingPort,
            LorebookVectorSearchPort vectorSearchPort,
            ChronicleVectorSearchPort chronicleVectorSearchPort,
            ChronicleSegmentRepository chronicleSegmentRepository,
            ApplicationEventPublisher eventPublisher,
            @Value("${moirai.adventure.message-window-size}") int messageWindowSize,
            @Value("${moirai.rag.lorebook.top-k}") int lorebookTopK,
            @Value("${moirai.rag.chronicle.top-k}") int chronicleTopK) {

        this.adventureRepository = adventureRepository;
        this.personaRepository = personaRepository;
        this.messageRepository = messageRepository;
        this.textCompletionPort = textCompletionPort;
        this.embeddingPort = embeddingPort;
        this.vectorSearchPort = vectorSearchPort;
        this.chronicleVectorSearchPort = chronicleVectorSearchPort;
        this.chronicleSegmentRepository = chronicleSegmentRepository;
        this.eventPublisher = eventPublisher;
        this.messageWindowSize = messageWindowSize;
        this.lorebookTopK = lorebookTopK;
        this.chronicleTopK = chronicleTopK;
    }

    @Override
    public void validate(Retry command) {
        if (command.adventureId() == null) {
            throw new IllegalArgumentException("Adventure ID cannot be null");
        }
    }

    @Override
    public MessageResult execute(Retry command) {

        var adventure = adventureRepository.findByPublicId(command.adventureId())
                .orElseThrow(() -> new NotFoundException("Adventure not found"));

        var persona = personaRepository.findById(adventure.getPersonaId())
                .orElseThrow(() -> new NotFoundException("Persona not found"));

        var lastMessage = messageRepository.getLastActive(adventure.getId())
                .orElseThrow(() -> new BusinessRuleViolationException("Cannot retry: adventure has no messages"));

        if (lastMessage.getRole() != AiRole.ASSISTANT) {
            throw new BusinessRuleViolationException("Cannot retry: last message is not an AI response");
        }

        messageRepository.deleteLastAssistantMessage(adventure.getId());

        var history = messageRepository.findActiveByAdventureId(adventure.getId(), messageWindowSize);

        var embeddingInput = messageRepository.getLastActive(adventure.getId())
                .map(Message::getContent)
                .orElse("");

        var personality = replacePersonaNamePlaceholderWith(persona.getName())
                .apply(persona.getPersonality());

        var context = assembleContext(adventure, history, embeddingInput);
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
            List<Message> history,
            String currentMessage) {

        var context = new ArrayList<ChatMessage>();
        var contextAttributes = adventure.getContextAttributes();

        var queryVector = embeddingPort.embed(currentMessage);

        context.addAll(retrieveLorebookContext(adventure, queryVector));
        context.addAll(retrieveChronicleContext(adventure.getPublicId(), queryVector));

        context.addAll(interleaveBumps(
                history.stream().map(this::toChatMessage).toList(),
                contextAttributes.bump(),
                contextAttributes.bumpFrequency()));

        if (contextAttributes.authorsNote() != null && !contextAttributes.authorsNote().isBlank()) {
            context.add(ChatMessage.asSystem(contextAttributes.authorsNote()));
        }

        if (contextAttributes.scene() != null && !contextAttributes.scene().isBlank()) {
            context.add(ChatMessage.asSystem(formatScene().apply(contextAttributes.scene())));
        }

        if (contextAttributes.nudge() != null && !contextAttributes.nudge().isBlank()) {
            context.add(ChatMessage.asSystem(contextAttributes.nudge()));
        }

        return Collections.unmodifiableList(context);
    }

    private List<ChatMessage> retrieveLorebookContext(Adventure adventure, float[] queryVector) {

        var entryIds = vectorSearchPort.search(adventure.getPublicId(), queryVector, lorebookTopK);

        if (entryIds.isEmpty()) {
            return List.of();
        }

        return adventure.getLorebook().stream()
                .filter(e -> entryIds.contains(e.getPublicId()))
                .map(e -> ChatMessage.asSystem(e.getName() + ": " + e.getDescription()))
                .toList();
    }

    private List<ChatMessage> retrieveChronicleContext(UUID adventurePublicId, float[] queryVector) {

        var segmentIds = chronicleVectorSearchPort.search(adventurePublicId, queryVector, chronicleTopK);

        if (segmentIds.isEmpty()) {
            return List.of();
        }

        var segments = chronicleSegmentRepository.getAllByIds(segmentIds);

        return segments.stream()
                .map(s -> ChatMessage.asSystem(s.getContent()))
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

    private ChatMessage toChatMessage(Message message) {
        return switch (message.getRole()) {
            case USER -> ChatMessage.asUser(message.getContent());
            case ASSISTANT -> ChatMessage.asAssistant(message.getContent());
            default -> throw new BusinessRuleViolationException("Unexpected role in message history: " + message.getRole());
        };
    }
}
