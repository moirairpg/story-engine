package me.moirai.storyengine.core.application.command.adventure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import me.moirai.storyengine.common.enums.MessageAuthorRole;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.message.Message;
import me.moirai.storyengine.core.domain.message.MessageStatus;
import me.moirai.storyengine.core.port.inbound.chronicle.UpdateChronicle;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.adventure.ChronicleVectorSearchPort;
import me.moirai.storyengine.core.port.outbound.generation.EmbeddingPort;
import me.moirai.storyengine.core.port.outbound.generation.TextCompletionPort;
import me.moirai.storyengine.core.port.outbound.generation.TextGenerationResult;
import me.moirai.storyengine.core.port.outbound.message.MessageRepository;

@ExtendWith(MockitoExtension.class)
public class UpdateChronicleHandlerTest {

    @Mock
    private AdventureRepository adventureRepository;

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
    public void shouldGenerateAndSaveChronicleSegmentFromChronicledMessages() {

        // given
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);

        var command = new UpdateChronicle(UUID.randomUUID());

        when(adventureRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(messageRepository.findLatestChronicledByAdventureId(anyLong(), anyInt())).thenReturn(chronicledMessages(6));
        when(textCompletionPort.generateTextFrom(any()))
                .thenReturn(TextGenerationResult.builder().outputText("Chronicle summary").build());
        when(adventureRepository.save(any(Adventure.class))).thenReturn(adventure);
        when(embeddingPort.embed(anyString())).thenReturn(new float[] { 0.1f, 0.2f });

        // when
        handler.handle(command);

        // then
        verify(adventureRepository, times(1)).save(any(Adventure.class));
        verify(textCompletionPort, times(1)).generateTextFrom(any());
    }

    @Test
    public void shouldUpsertVectorForGeneratedSegment() {

        // given
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", AdventureFixture.PUBLIC_ID);

        var command = new UpdateChronicle(UUID.randomUUID());
        var vector = new float[] { 0.1f, 0.2f };

        when(adventureRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(messageRepository.findLatestChronicledByAdventureId(anyLong(), anyInt())).thenReturn(chronicledMessages(6));
        when(textCompletionPort.generateTextFrom(any()))
                .thenReturn(TextGenerationResult.builder().outputText("Chronicle summary").build());
        when(adventureRepository.save(any(Adventure.class))).thenReturn(adventure);
        when(embeddingPort.embed(anyString())).thenReturn(vector);

        var vectorCaptor = ArgumentCaptor.forClass(float[].class);

        // when
        handler.handle(command);

        // then
        verify(chronicleVectorSearchPort, times(1)).upsert(any(UUID.class), any(UUID.class), vectorCaptor.capture());
        assertThat(vectorCaptor.getValue()).isEqualTo(vector);
    }

    @Test
    public void shouldRequestChronicledMessagesUsingWindowPlusOne() {

        // given
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);

        var command = new UpdateChronicle(UUID.randomUUID());

        when(adventureRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(messageRepository.findLatestChronicledByAdventureId(anyLong(), anyInt())).thenReturn(chronicledMessages(6));
        when(textCompletionPort.generateTextFrom(any()))
                .thenReturn(TextGenerationResult.builder().outputText("Chronicle summary").build());
        when(adventureRepository.save(any(Adventure.class))).thenReturn(adventure);
        when(embeddingPort.embed(anyString())).thenReturn(new float[] { 0.1f, 0.2f });

        var limitCaptor = ArgumentCaptor.forClass(Integer.class);

        // when
        handler.handle(command);

        // then
        verify(messageRepository).findLatestChronicledByAdventureId(anyLong(), limitCaptor.capture());
        assertThat(limitCaptor.getValue()).isEqualTo(6);
    }

    private Message messageData(MessageAuthorRole role) {
        return Message.builder()
                .adventureId(1L)
                .role(role)
                .content("Some content")
                .status(MessageStatus.CHRONICLED)
                .build();
    }

    private List<Message> chronicledMessages(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> messageData(i % 2 == 0 ? MessageAuthorRole.USER : MessageAuthorRole.ASSISTANT))
                .toList();
    }
}
