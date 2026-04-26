package me.moirai.storyengine.core.application.query.adventure;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import me.moirai.storyengine.common.annotation.QueryHandler;
import me.moirai.storyengine.common.cqs.query.AbstractQueryHandler;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureCatchUp;
import me.moirai.storyengine.core.port.inbound.adventure.CatchUpResult;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureReader;
import me.moirai.storyengine.core.port.outbound.adventure.ChronicleSegmentReader;
import me.moirai.storyengine.core.port.outbound.generation.ChatMessage;
import me.moirai.storyengine.core.port.outbound.generation.TextCompletionPort;
import me.moirai.storyengine.core.port.outbound.generation.TextGenerationRequest;
import me.moirai.storyengine.core.port.outbound.message.MessageReader;

@QueryHandler
public class AdventureCatchUpHandler
        extends AbstractQueryHandler<AdventureCatchUp, CatchUpResult> {

    private static final String CATCHUP_INSTRUCTION = "Provide a brief in-world recap of the story so far.";
    private static final String HISTORY_SEPARATOR = "\n\n";

    private final AdventureReader adventureReader;
    private final ChronicleSegmentReader chronicleSegmentReader;
    private final MessageReader messageReader;
    private final TextCompletionPort textCompletionPort;

    public AdventureCatchUpHandler(
            AdventureReader adventureReader,
            ChronicleSegmentReader chronicleSegmentReader,
            MessageReader messageReader,
            TextCompletionPort textCompletionPort) {

        this.adventureReader = adventureReader;
        this.chronicleSegmentReader = chronicleSegmentReader;
        this.messageReader = messageReader;
        this.textCompletionPort = textCompletionPort;
    }

    @Override
    public void validate(AdventureCatchUp query) {
        if (query.adventureId() == null) {
            throw new IllegalArgumentException("Adventure ID cannot be null");
        }
    }

    @Override
    public CatchUpResult execute(AdventureCatchUp query) {

        var adventure = adventureReader.getAdventureById(query.adventureId())
                .orElseThrow(() -> new NotFoundException("Adventure not found"));

        var segments = chronicleSegmentReader.getAllOrdered(adventure.id());
        var activeMessages = messageReader.getAllActiveByAdventureId(adventure.id());

        if (segments.isEmpty() && activeMessages.isEmpty()) {
            return new CatchUpResult("");
        }

        var segmentHistory = segments.stream()
                .map(s -> s.content())
                .collect(Collectors.joining(HISTORY_SEPARATOR));

        var messageHistory = activeMessages.stream()
                .map(m -> m.content())
                .collect(Collectors.joining(HISTORY_SEPARATOR));

        var storyHistory = Stream.of(segmentHistory, messageHistory)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(HISTORY_SEPARATOR));

        var modelConfig = adventure.modelConfiguration();

        var request = new TextGenerationRequest(
                modelConfig.aiModel().getOfficialModelName(),
                CATCHUP_INSTRUCTION,
                List.of(ChatMessage.asUser(storyHistory)),
                modelConfig.maxTokenLimit(),
                modelConfig.temperature());

        var result = textCompletionPort.generateTextFrom(request);

        return new CatchUpResult(result.getOutputText());
    }
}
