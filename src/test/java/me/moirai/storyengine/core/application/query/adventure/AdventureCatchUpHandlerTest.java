package me.moirai.storyengine.core.application.query.adventure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.enums.AiRole;
import me.moirai.storyengine.common.enums.ArtificialIntelligenceModel;
import me.moirai.storyengine.common.enums.Moderation;
import me.moirai.storyengine.common.enums.Visibility;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.domain.message.MessageStatus;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureCatchUp;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureDetails;
import me.moirai.storyengine.core.port.inbound.adventure.ContextAttributesDto;
import me.moirai.storyengine.core.port.inbound.adventure.ModelConfigurationDto;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureReader;
import me.moirai.storyengine.core.port.outbound.chronicle.ChronicleSegmentData;
import me.moirai.storyengine.core.port.outbound.chronicle.ChronicleSegmentReader;
import me.moirai.storyengine.core.port.outbound.generation.TextCompletionPort;
import me.moirai.storyengine.core.port.outbound.generation.TextGenerationResult;
import me.moirai.storyengine.core.port.outbound.message.MessageData;
import me.moirai.storyengine.core.port.outbound.message.MessageReader;

@ExtendWith(MockitoExtension.class)
public class AdventureCatchUpHandlerTest {

    @Mock
    private AdventureReader adventureReader;

    @Mock
    private ChronicleSegmentReader chronicleSegmentReader;

    @Mock
    private MessageReader messageReader;

    @Mock
    private TextCompletionPort textCompletionPort;

    private AdventureCatchUpHandler handler;

    @BeforeEach
    void setup() {
        handler = new AdventureCatchUpHandler(adventureReader, chronicleSegmentReader, messageReader, textCompletionPort);
    }

    @Test
    public void shouldThrowWhenAdventureIdIsNull() {

        // given
        var query = new AdventureCatchUp(null);

        // when / then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(query));
    }

    @Test
    public void shouldThrowWhenAdventureNotFound() {

        // given
        var query = new AdventureCatchUp(UUID.randomUUID());
        when(adventureReader.getAdventureById(any(UUID.class))).thenReturn(Optional.empty());

        // when / then
        assertThrows(NotFoundException.class, () -> handler.handle(query));
    }

    @Test
    public void shouldReturnEmptyResultWhenNoHistoryAtAll() {

        // given
        var adventureId = UUID.randomUUID();
        var adventure = buildAdventureDetails(adventureId);
        var query = new AdventureCatchUp(adventureId);

        when(adventureReader.getAdventureById(adventureId)).thenReturn(Optional.of(adventure));
        when(chronicleSegmentReader.getAllOrdered(adventureId)).thenReturn(List.of());
        when(messageReader.getAllActiveByAdventureId(adventureId)).thenReturn(List.of());

        // when
        var result = handler.handle(query);

        // then
        assertThat(result.summary()).isEmpty();
        verify(textCompletionPort, never()).generateTextFrom(any());
    }

    @Test
    public void shouldGenerateSummaryFromSegmentsAlone() {

        // given
        var adventureId = UUID.randomUUID();
        var adventure = buildAdventureDetails(adventureId);
        var query = new AdventureCatchUp(adventureId);

        var segment = new ChronicleSegmentData(UUID.randomUUID(), 1L, "The hero slew the dragon.", null);
        var generationResult = TextGenerationResult.builder().outputText("A great battle occurred.").build();

        when(adventureReader.getAdventureById(adventureId)).thenReturn(Optional.of(adventure));
        when(chronicleSegmentReader.getAllOrdered(adventureId)).thenReturn(List.of(segment));
        when(messageReader.getAllActiveByAdventureId(adventureId)).thenReturn(List.of());
        when(textCompletionPort.generateTextFrom(any())).thenReturn(generationResult);

        // when
        var result = handler.handle(query);

        // then
        assertThat(result.summary()).isEqualTo("A great battle occurred.");
        verify(textCompletionPort).generateTextFrom(any());
    }

    @Test
    public void shouldGenerateSummaryFromActiveMessagesAlone() {

        // given
        var adventureId = UUID.randomUUID();
        var adventure = buildAdventureDetails(adventureId);
        var query = new AdventureCatchUp(adventureId);

        var message = new MessageData(UUID.randomUUID(), 1L, "user", AiRole.USER, "I look around.", null, MessageStatus.ACTIVE);
        var generationResult = TextGenerationResult.builder().outputText("You looked around.").build();

        when(adventureReader.getAdventureById(adventureId)).thenReturn(Optional.of(adventure));
        when(chronicleSegmentReader.getAllOrdered(adventureId)).thenReturn(List.of());
        when(messageReader.getAllActiveByAdventureId(adventureId)).thenReturn(List.of(message));
        when(textCompletionPort.generateTextFrom(any())).thenReturn(generationResult);

        // when
        var result = handler.handle(query);

        // then
        assertThat(result.summary()).isEqualTo("You looked around.");
        verify(textCompletionPort).generateTextFrom(any());
    }

    @Test
    public void shouldCombineSegmentsAndMessagesInOrder() {

        // given
        var adventureId = UUID.randomUUID();
        var adventure = buildAdventureDetails(adventureId);
        var query = new AdventureCatchUp(adventureId);

        var segment = new ChronicleSegmentData(UUID.randomUUID(), 1L, "Chronicle: hero began journey.", null);
        var message = new MessageData(UUID.randomUUID(), 1L, "user", AiRole.USER, "I arrive at the city.", null, MessageStatus.ACTIVE);
        var generationResult = TextGenerationResult.builder().outputText("Combined recap.").build();

        when(adventureReader.getAdventureById(adventureId)).thenReturn(Optional.of(adventure));
        when(chronicleSegmentReader.getAllOrdered(adventureId)).thenReturn(List.of(segment));
        when(messageReader.getAllActiveByAdventureId(adventureId)).thenReturn(List.of(message));
        when(textCompletionPort.generateTextFrom(any())).thenReturn(generationResult);

        // when
        var result = handler.handle(query);

        // then
        assertThat(result.summary()).isEqualTo("Combined recap.");
        verify(textCompletionPort).generateTextFrom(any());
    }

    private AdventureDetails buildAdventureDetails(UUID adventureId) {
        var modelConfig = new ModelConfigurationDto(ArtificialIntelligenceModel.GPT54_MINI, 1000, 0.7);
        var contextAttributes = new ContextAttributesDto(null, null, null, null, 0);
        return new AdventureDetails(
                adventureId,
                "Test Adventure",
                "Description",
                "Adventure start",
                UUID.randomUUID(),
                null,
                null,
                Visibility.PRIVATE,
                Moderation.STRICT,
                false,
                null,
                null,
                null,
                modelConfig,
                contextAttributes,
                Set.of(),
                Set.of());
    }
}
