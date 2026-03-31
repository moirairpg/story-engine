package me.moirai.storyengine.core.application.command.chronicle;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.domain.chronicle.ChronicleSegment;
import me.moirai.storyengine.core.port.inbound.chronicle.UpdateChronicle;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.chronicle.ChronicleSegmentRepository;
import me.moirai.storyengine.core.port.outbound.generation.ChatMessage;
import me.moirai.storyengine.core.port.outbound.generation.EmbeddingPort;
import me.moirai.storyengine.core.port.outbound.generation.TextCompletionPort;
import me.moirai.storyengine.core.port.outbound.generation.TextGenerationRequest;
import me.moirai.storyengine.core.port.outbound.message.MessageData;
import me.moirai.storyengine.core.port.outbound.message.MessageReader;
import me.moirai.storyengine.core.port.outbound.message.MessageRepository;
import me.moirai.storyengine.core.port.outbound.vectorsearch.ChronicleVectorSearchPort;

@CommandHandler
public class UpdateChronicleHandler extends AbstractCommandHandler<UpdateChronicle, Void> {

    private static final String CHRONICLE_PROMPT = """
            Write a concise narrative chronicle of the story events in the messages below.
            Preserve key facts, character actions, consequences, and plot developments.
            Write in the style of story notes, not a conversation transcript.
            """;

    private final AdventureRepository adventureRepository;
    private final MessageReader messageReader;
    private final MessageRepository messageRepository;
    private final TextCompletionPort textCompletionPort;
    private final EmbeddingPort embeddingPort;
    private final ChronicleSegmentRepository chronicleSegmentRepository;
    private final ChronicleVectorSearchPort chronicleVectorSearchPort;
    private final int messageWindowSize;

    public UpdateChronicleHandler(
            AdventureRepository adventureRepository,
            MessageReader messageReader,
            MessageRepository messageRepository,
            TextCompletionPort textCompletionPort,
            EmbeddingPort embeddingPort,
            ChronicleSegmentRepository chronicleSegmentRepository,
            ChronicleVectorSearchPort chronicleVectorSearchPort,
            @Value("${moirai.adventure.message-window-size}") int messageWindowSize) {

        this.adventureRepository = adventureRepository;
        this.messageReader = messageReader;
        this.messageRepository = messageRepository;
        this.textCompletionPort = textCompletionPort;
        this.embeddingPort = embeddingPort;
        this.chronicleSegmentRepository = chronicleSegmentRepository;
        this.chronicleVectorSearchPort = chronicleVectorSearchPort;
        this.messageWindowSize = messageWindowSize;
    }

    @Override
    public void validate(UpdateChronicle command) {

        if (command.adventurePublicId() == null) {
            throw new IllegalArgumentException("Adventure public ID cannot be null");
        }
    }

    @Override
    public Void execute(UpdateChronicle command) {

        var adventure = adventureRepository.findByPublicId(command.adventurePublicId())
                .orElseThrow(() -> new NotFoundException("Adventure not found"));

        var allActive = messageReader.getAllActiveByAdventureId(adventure.getPublicId());

        if (allActive.size() <= messageWindowSize) {
            return null;
        }

        var messagesToChronicle = allActive.subList(0, allActive.size() - messageWindowSize);

        var chronicleInput = messagesToChronicle.stream()
                .map(m -> m.role().name() + ": " + m.content())
                .collect(Collectors.joining("\n"));

        var modelConfig = adventure.getModelConfiguration();

        var request = new TextGenerationRequest(
                modelConfig.getAiModel().getOfficialModelName(),
                CHRONICLE_PROMPT,
                List.of(ChatMessage.asUser(chronicleInput)),
                modelConfig.getMaxTokenLimit(),
                modelConfig.getTemperature());

        var segmentContent = textCompletionPort.generateTextFrom(request).getOutputText();

        var savedSegment = chronicleSegmentRepository.save(ChronicleSegment.builder()
                .adventureId(adventure.getId())
                .content(segmentContent)
                .build());

        var vector = embeddingPort.embed(segmentContent);
        chronicleVectorSearchPort.upsert(adventure.getPublicId(), savedSegment.getPublicId(), vector);

        var chronicleIds = messagesToChronicle.stream()
                .map(MessageData::publicId)
                .toList();

        messageRepository.markAsChronicled(chronicleIds);

        return null;
    }
}
