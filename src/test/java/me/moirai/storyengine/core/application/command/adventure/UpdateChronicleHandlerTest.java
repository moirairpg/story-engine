package me.moirai.storyengine.core.application.command.adventure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import me.moirai.storyengine.common.enums.AiRole;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.chronicle.ChronicleSegmentFixture;
import me.moirai.storyengine.core.domain.message.MessageStatus;
import me.moirai.storyengine.core.port.inbound.chronicle.UpdateChronicle;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.generation.EmbeddingPort;
import me.moirai.storyengine.core.port.outbound.generation.TextCompletionPort;
import me.moirai.storyengine.core.port.outbound.generation.TextGenerationResult;
import me.moirai.storyengine.core.port.outbound.message.MessageData;
import me.moirai.storyengine.core.port.outbound.message.MessageReader;
import me.moirai.storyengine.core.port.outbound.message.MessageRepository;
import me.moirai.storyengine.core.port.outbound.vectorsearch.ChronicleVectorSearchPort;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
public class UpdateChronicleHandlerTest {

    @Mock
    private AdventureRepository adventureRepository;

    @Mock
    private MessageReader messageReader;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private TextCompletionPort textCompletionPort;

    @Mock
    private EmbeddingPort embeddingPort;

    @Mock
    private ChronicleVectorSearchPort chronicleVectorSearchPort;

    private UpdateChronicleHandler handler;

    @BeforeEach
    void setup() {
        handler = new UpdateChronicleHandler(
                adventureRepository,
                messageReader,
                messageRepository,
                textCompletionPort,
                embeddingPort,
                chronicleVectorSearchPort,
                5);
    }

    @Test
    public void shouldThrowWhenAdventurePublicIdIsNull() {

        // given
        var command = new UpdateChronicle(null);

        // when / then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void shouldThrowWhenAdventureNotFound() {

        // given
        var command = new UpdateChronicle(UUID.randomUUID());
        when(adventureRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

        // when / then
        assertThrows(NotFoundException.class, () -> handler.handle(command));
    }

    @Test
    public void shouldReturnNullImmediatelyWhenNoMessagesSpillOverWindow() {

        // given
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);

        var command = new UpdateChronicle(UUID.randomUUID());

        when(adventureRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(messageReader.getAllActiveByAdventureId(any(UUID.class))).thenReturn(List.of(
                messageData(AiRole.USER),
                messageData(AiRole.ASSISTANT),
                messageData(AiRole.USER)));

        // when
        var result = handler.handle(command);

        // then
        assertThat(result).isNull();
        verify(textCompletionPort, never()).generateTextFrom(any());
        verify(adventureRepository, never()).save(any());
    }

    @Test
    public void shouldSaveChronicleSegmentWhenMessagesSpillOverWindow() {

        // given
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);

        var savedSegment = ChronicleSegmentFixture.chronicleSegment().build();
        adventure.addChronicleSegment(savedSegment.getContent());

        var command = new UpdateChronicle(UUID.randomUUID());

        when(adventureRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(messageReader.getAllActiveByAdventureId(any(UUID.class))).thenReturn(overflowingMessages(7));
        when(textCompletionPort.generateTextFrom(any())).thenReturn(TextGenerationResult.builder().outputText("Chronicle summary").build());
        when(adventureRepository.save(any(Adventure.class))).thenReturn(adventure);
        when(embeddingPort.embed(anyString())).thenReturn(new float[] { 0.1f, 0.2f });

        // when
        handler.handle(command);

        // then
        verify(adventureRepository).save(any(Adventure.class));
    }

    @Test
    public void shouldUpsertVectorForSavedSegment() {

        // given
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);

        var savedSegment = ChronicleSegmentFixture.chronicleSegment().build();
        adventure.addChronicleSegment(savedSegment.getContent());

        var command = new UpdateChronicle(UUID.randomUUID());
        var vector = new float[] { 0.1f, 0.2f };

        when(adventureRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(messageReader.getAllActiveByAdventureId(any(UUID.class))).thenReturn(overflowingMessages(7));
        when(textCompletionPort.generateTextFrom(any())).thenReturn(TextGenerationResult.builder().outputText("Chronicle summary").build());
        when(adventureRepository.save(any(Adventure.class))).thenReturn(adventure);
        when(embeddingPort.embed(anyString())).thenReturn(vector);

        // when
        handler.handle(command);

        // then
        verify(adventureRepository, times(1)).save(adventure);
    }

    @Test
    public void shouldMarkChronicledMessagesAsChronicled() {

        // given
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);

        var savedSegment = ChronicleSegmentFixture.chronicleSegment().build();
        adventure.addChronicleSegment(savedSegment.getContent());

        var command = new UpdateChronicle(UUID.randomUUID());

        when(adventureRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(messageReader.getAllActiveByAdventureId(any(UUID.class))).thenReturn(overflowingMessages(7));
        when(textCompletionPort.generateTextFrom(any())).thenReturn(TextGenerationResult.builder().outputText("Chronicle summary").build());
        when(adventureRepository.save(any(Adventure.class))).thenReturn(adventure);
        when(embeddingPort.embed(anyString())).thenReturn(new float[] { 0.1f, 0.2f });

        var captor = ArgumentCaptor.forClass(List.class);

        // when
        handler.handle(command);

        // then
        verify(messageRepository).markAsChronicled(captor.capture());
        assertThat(captor.getValue()).hasSize(2);
    }

    @Test
    public void shouldOnlyChronicleMessagesOutsideWindow() {

        // given
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", AdventureFixture.PUBLIC_ID);

        var savedSegment = ChronicleSegmentFixture.chronicleSegment().build();
        adventure.addChronicleSegment(savedSegment.getContent());

        var command = new UpdateChronicle(UUID.randomUUID());
        var messages = overflowingMessages(8);

        when(adventureRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(messageReader.getAllActiveByAdventureId(any(UUID.class))).thenReturn(messages);
        when(textCompletionPort.generateTextFrom(any())).thenReturn(TextGenerationResult.builder().outputText("Chronicle summary").build());
        when(adventureRepository.save(any(Adventure.class))).thenReturn(adventure);
        when(embeddingPort.embed(anyString())).thenReturn(new float[] { 0.1f, 0.2f });

        var captor = ArgumentCaptor.forClass(List.class);

        // when
        handler.handle(command);

        // then
        verify(messageRepository).markAsChronicled(captor.capture());
        assertThat(captor.getValue()).hasSize(3);
    }

    private MessageData messageData(AiRole role) {
        return new MessageData(UUID.randomUUID(), 1L, "SYSTEM", role, "Some content", null, MessageStatus.ACTIVE);
    }

    private List<MessageData> overflowingMessages(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> messageData(i % 2 == 0 ? AiRole.USER : AiRole.ASSISTANT))
                .toList();
    }
}
