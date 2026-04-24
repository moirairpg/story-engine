package me.moirai.storyengine.core.application.command.message;

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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import me.moirai.storyengine.common.enums.AiRole;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.chronicle.ChronicleSegment;
import me.moirai.storyengine.core.domain.message.Message;
import me.moirai.storyengine.core.domain.message.MessageFixture;
import me.moirai.storyengine.core.port.inbound.message.SendMessage;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.chronicle.ChronicleSegmentRepository;
import me.moirai.storyengine.core.port.outbound.generation.EmbeddingPort;
import me.moirai.storyengine.core.port.outbound.generation.TextCompletionPort;
import me.moirai.storyengine.core.port.outbound.generation.TextGenerationRequest;
import me.moirai.storyengine.core.port.outbound.generation.TextGenerationResult;
import me.moirai.storyengine.core.port.outbound.message.MessageRepository;
import me.moirai.storyengine.core.port.outbound.vectorsearch.ChronicleVectorSearchPort;
import me.moirai.storyengine.core.port.outbound.vectorsearch.LorebookVectorSearchPort;

@ExtendWith(MockitoExtension.class)
public class SendMessageHandlerTest {

    @Mock
    private AdventureRepository adventureRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private TextCompletionPort textCompletionPort;

    @Mock
    private EmbeddingPort embeddingPort;

    @Mock
    private LorebookVectorSearchPort vectorSearchPort;

    @Mock
    private ChronicleVectorSearchPort chronicleVectorSearchPort;

    @Mock
    private ChronicleSegmentRepository chronicleSegmentRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private SendMessageHandler handler;

    @BeforeEach
    void setUp() {

        handler = new SendMessageHandler(
                adventureRepository,
                messageRepository,
                textCompletionPort,
                embeddingPort,
                vectorSearchPort,
                chronicleVectorSearchPort,
                chronicleSegmentRepository,
                eventPublisher,
                10,
                5,
                3);
    }

    @Test
    public void shouldThrowWhenAdventureNotFound() {

        // given
        var command = new SendMessage(UUID.randomUUID(), "Hello!", "user");
        when(adventureRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

        // when / then
        assertThrows(NotFoundException.class, () -> handler.handle(command));
    }

    @Test
    public void shouldSavePlayerMessageBeforeGeneratingResponse() {

        // given
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", AdventureFixture.PUBLIC_ID);

        var savedMessage = MessageFixture.assistantMessage().build();
        ReflectionTestUtils.setField(savedMessage, "publicId", UUID.randomUUID());

        var generationResult = TextGenerationResult.builder()
                .outputText("AI response")
                .build();

        var command = new SendMessage(UUID.randomUUID(), "Hello!", "user");

        when(adventureRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);
        when(messageRepository.findActiveByAdventureId(anyLong(), anyInt())).thenReturn(List.of());
        when(embeddingPort.embed(anyString())).thenReturn(new float[] { 0.1f, 0.2f });
        when(vectorSearchPort.search(any(UUID.class), any(float[].class), anyInt())).thenReturn(List.of());
        when(textCompletionPort.generateTextFrom(any())).thenReturn(generationResult);

        // when
        var result = handler.handle(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo("AI response");
    }

    @Test
    public void shouldSaveAiMessageAfterGeneratingResponse() {

        // given
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", AdventureFixture.PUBLIC_ID);

        var savedMessage = MessageFixture.assistantMessage().build();
        ReflectionTestUtils.setField(savedMessage, "publicId", UUID.randomUUID());

        var generationResult = TextGenerationResult.builder()
                .outputText("AI response")
                .build();

        var command = new SendMessage(UUID.randomUUID(), "Hello!", "user");

        when(adventureRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);
        when(messageRepository.findActiveByAdventureId(anyLong(), anyInt())).thenReturn(List.of());
        when(embeddingPort.embed(anyString())).thenReturn(new float[] { 0.1f, 0.2f });
        when(vectorSearchPort.search(any(UUID.class), any(float[].class), anyInt())).thenReturn(List.of());
        when(textCompletionPort.generateTextFrom(any())).thenReturn(generationResult);

        // when
        var result = handler.handle(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.role()).isEqualTo(savedMessage.getRole());
    }

    @Test
    public void shouldPrependLorebookEntriesWhenVectorSearchReturnsResults() {

        // given
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", AdventureFixture.PUBLIC_ID);

        var savedMessage = MessageFixture.assistantMessage().build();
        ReflectionTestUtils.setField(savedMessage, "publicId", UUID.randomUUID());

        var generationResult = TextGenerationResult.builder()
                .outputText("AI response")
                .build();

        var entryId = UUID.randomUUID();
        var lorebookEntry = adventure.addLorebookEntry("Dragon", "A fearsome dragon", null);
        ReflectionTestUtils.setField(lorebookEntry, "publicId", entryId);

        var command = new SendMessage(UUID.randomUUID(), "Hello!", "user");

        when(adventureRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);
        when(messageRepository.findActiveByAdventureId(anyLong(), anyInt())).thenReturn(List.of());
        when(embeddingPort.embed(anyString())).thenReturn(new float[] { 0.1f, 0.2f });
        when(vectorSearchPort.search(any(UUID.class), any(float[].class), anyInt())).thenReturn(List.of(entryId));
        when(textCompletionPort.generateTextFrom(any())).thenReturn(generationResult);

        // when
        var result = handler.handle(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo("AI response");
    }

    @Test
    public void shouldNotPrependLorebookEntriesWhenVectorSearchReturnsEmptyList() {

        // given
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", AdventureFixture.PUBLIC_ID);

        var savedMessage = MessageFixture.assistantMessage().build();
        ReflectionTestUtils.setField(savedMessage, "publicId", UUID.randomUUID());

        var generationResult = TextGenerationResult.builder()
                .outputText("AI response")
                .build();

        var command = new SendMessage(UUID.randomUUID(), "Hello!", "user");

        when(adventureRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);
        when(messageRepository.findActiveByAdventureId(anyLong(), anyInt())).thenReturn(List.of());
        when(embeddingPort.embed(anyString())).thenReturn(new float[] { 0.1f, 0.2f });
        when(vectorSearchPort.search(any(UUID.class), any(float[].class), anyInt())).thenReturn(List.of());
        when(textCompletionPort.generateTextFrom(any())).thenReturn(generationResult);

        // when
        var result = handler.handle(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo("AI response");
    }

    @Test
    public void shouldNotPrependLorebookEntriesWhenNoMatchingEntryInLorebook() {

        // given
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", AdventureFixture.PUBLIC_ID);

        var savedMessage = MessageFixture.assistantMessage().build();
        ReflectionTestUtils.setField(savedMessage, "publicId", UUID.randomUUID());

        var generationResult = TextGenerationResult.builder()
                .outputText("AI response")
                .build();

        var entryId = UUID.randomUUID();
        var command = new SendMessage(UUID.randomUUID(), "Hello!", "user");

        when(adventureRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);
        when(messageRepository.findActiveByAdventureId(anyLong(), anyInt())).thenReturn(List.of());
        when(embeddingPort.embed(anyString())).thenReturn(new float[] { 0.1f, 0.2f });
        when(vectorSearchPort.search(any(UUID.class), any(float[].class), anyInt())).thenReturn(List.of(entryId));
        when(textCompletionPort.generateTextFrom(any())).thenReturn(generationResult);

        // when
        var result = handler.handle(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo("AI response");
    }

    @Test
    public void shouldThrowExceptionWhenAdventureIdIsNull() {

        // given
        var command = new SendMessage(null, "Hello!", "user");

        // when / then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void shouldThrowExceptionWhenContentIsBlank() {

        // given
        var command = new SendMessage(UUID.randomUUID(), "", "user");

        // when / then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void shouldSavePlayerMessageWithCharacterNamePrefix() {

        // given
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", AdventureFixture.PUBLIC_ID);

        adventure.addLorebookEntry("Aldric", "A brave warrior", "user");

        var savedMessage = MessageFixture.assistantMessage().build();
        ReflectionTestUtils.setField(savedMessage, "publicId", UUID.randomUUID());

        var generationResult = TextGenerationResult.builder()
                .outputText("AI response")
                .build();

        var command = new SendMessage(UUID.randomUUID(), "Hello!", "user");

        when(adventureRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);
        when(messageRepository.findActiveByAdventureId(anyLong(), anyInt())).thenReturn(List.of());
        when(embeddingPort.embed(anyString())).thenReturn(new float[] { 0.1f, 0.2f });
        when(vectorSearchPort.search(any(UUID.class), any(float[].class), anyInt())).thenReturn(List.of());
        when(textCompletionPort.generateTextFrom(any())).thenReturn(generationResult);

        var captor = ArgumentCaptor.forClass(Message.class);

        // when
        handler.handle(command);

        // then
        verify(messageRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues().get(0).getContent()).isEqualTo("Aldric said: Hello!");
    }

    @Test
    public void shouldFallBackToUsernameWhenNoCharacterEntryFound() {

        // given
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", AdventureFixture.PUBLIC_ID);

        var savedMessage = MessageFixture.assistantMessage().build();
        ReflectionTestUtils.setField(savedMessage, "publicId", UUID.randomUUID());

        var generationResult = TextGenerationResult.builder()
                .outputText("AI response")
                .build();

        var command = new SendMessage(UUID.randomUUID(), "Hello!", "user");

        when(adventureRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);
        when(messageRepository.findActiveByAdventureId(anyLong(), anyInt())).thenReturn(List.of());
        when(embeddingPort.embed(anyString())).thenReturn(new float[] { 0.1f, 0.2f });
        when(vectorSearchPort.search(any(UUID.class), any(float[].class), anyInt())).thenReturn(List.of());
        when(textCompletionPort.generateTextFrom(any())).thenReturn(generationResult);

        var captor = ArgumentCaptor.forClass(Message.class);

        // when
        handler.handle(command);

        // then
        verify(messageRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues().get(0).getContent()).isEqualTo("user said: Hello!");
    }

    @Test
    public void shouldPassHistoryMessagesAsIsInContext() {

        // given
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", AdventureFixture.PUBLIC_ID);

        var savedMessage = MessageFixture.assistantMessage().build();
        ReflectionTestUtils.setField(savedMessage, "publicId", UUID.randomUUID());

        var generationResult = TextGenerationResult.builder()
                .outputText("AI response")
                .build();

        var userMessage = Message.builder()
                .adventureId(AdventureFixture.NUMERIC_ID)
                .role(AiRole.USER)
                .content("Aldric said: I look around.")
                .build();
        var assistantMessage = Message.builder()
                .adventureId(AdventureFixture.NUMERIC_ID)
                .role(AiRole.ASSISTANT)
                .content("MoirAI said: You see a tavern.")
                .build();

        var command = new SendMessage(UUID.randomUUID(), "Hello!", "user");

        when(adventureRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);
        when(messageRepository.findActiveByAdventureId(anyLong(), anyInt()))
                .thenReturn(List.of(userMessage, assistantMessage));
        when(embeddingPort.embed(anyString())).thenReturn(new float[] { 0.1f, 0.2f });
        when(vectorSearchPort.search(any(UUID.class), any(float[].class), anyInt())).thenReturn(List.of());
        when(textCompletionPort.generateTextFrom(any())).thenReturn(generationResult);

        var captor = ArgumentCaptor.forClass(TextGenerationRequest.class);

        // when
        handler.handle(command);

        // then
        verify(textCompletionPort, times(2)).generateTextFrom(captor.capture());
        var messages = captor.getAllValues().get(1).messages();
        assertThat(messages).anySatisfy(m -> assertThat(m.content()).isEqualTo("Aldric said: I look around."));
        assertThat(messages).anySatisfy(m -> assertThat(m.content()).isEqualTo("MoirAI said: You see a tavern."));
    }

    @Test
    public void shouldReplaceNamePlaceholderInNarratorPersonality() {

        // given
        var adventure = AdventureFixture.privateSingleplayerAdventure()
                .narrator("MoirAI", "I am {name}, a Discord chatbot")
                .build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", AdventureFixture.PUBLIC_ID);

        var savedMessage = MessageFixture.assistantMessage().build();
        ReflectionTestUtils.setField(savedMessage, "publicId", UUID.randomUUID());

        var generationResult = TextGenerationResult.builder()
                .outputText("AI response")
                .build();

        var command = new SendMessage(UUID.randomUUID(), "Hello!", "user");

        when(adventureRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);
        when(messageRepository.findActiveByAdventureId(anyLong(), anyInt())).thenReturn(List.of());
        when(embeddingPort.embed(anyString())).thenReturn(new float[] { 0.1f, 0.2f });
        when(vectorSearchPort.search(any(UUID.class), any(float[].class), anyInt())).thenReturn(List.of());
        when(textCompletionPort.generateTextFrom(any())).thenReturn(generationResult);

        var captor = ArgumentCaptor.forClass(TextGenerationRequest.class);

        // when
        handler.handle(command);

        // then
        verify(textCompletionPort, times(2)).generateTextFrom(captor.capture());
        assertThat(captor.getAllValues().get(1).instructions()).isEqualTo("I am MoirAI, a Discord chatbot");
    }

    @Test
    public void shouldStripChatPrefixFromAiResponseBeforeSaving() {

        // given
        var adventure = AdventureFixture.privateSingleplayerAdventure()
                .narrator("MoirAI", "I am a Discord chatbot")
                .build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", AdventureFixture.PUBLIC_ID);

        var savedMessage = MessageFixture.assistantMessage().build();
        ReflectionTestUtils.setField(savedMessage, "publicId", UUID.randomUUID());

        var generationResult = TextGenerationResult.builder()
                .outputText("MoirAI said: some text.")
                .build();

        var command = new SendMessage(UUID.randomUUID(), "Hello!", "user");

        when(adventureRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);
        when(messageRepository.findActiveByAdventureId(anyLong(), anyInt())).thenReturn(List.of());
        when(embeddingPort.embed(anyString())).thenReturn(new float[] { 0.1f, 0.2f });
        when(vectorSearchPort.search(any(UUID.class), any(float[].class), anyInt())).thenReturn(List.of());
        when(textCompletionPort.generateTextFrom(any())).thenReturn(generationResult);

        var captor = ArgumentCaptor.forClass(Message.class);

        // when
        handler.handle(command);

        // then
        verify(messageRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues().get(1).getContent()).isEqualTo("MoirAI said: some text.");
    }

    @Test
    public void shouldStripAsNamePrefixFromAiResponseBeforeSaving() {

        // given
        var adventure = AdventureFixture.privateSingleplayerAdventure()
                .narrator("MoirAI", "I am a Discord chatbot")
                .build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", AdventureFixture.PUBLIC_ID);

        var savedMessage = MessageFixture.assistantMessage().build();
        ReflectionTestUtils.setField(savedMessage, "publicId", UUID.randomUUID());

        var generationResult = TextGenerationResult.builder()
                .outputText("As MoirAI, She walks forward.")
                .build();

        var command = new SendMessage(UUID.randomUUID(), "Hello!", "user");

        when(adventureRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);
        when(messageRepository.findActiveByAdventureId(anyLong(), anyInt())).thenReturn(List.of());
        when(embeddingPort.embed(anyString())).thenReturn(new float[] { 0.1f, 0.2f });
        when(vectorSearchPort.search(any(UUID.class), any(float[].class), anyInt())).thenReturn(List.of());
        when(textCompletionPort.generateTextFrom(any())).thenReturn(generationResult);

        var captor = ArgumentCaptor.forClass(Message.class);

        // when
        handler.handle(command);

        // then
        verify(messageRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues().get(1).getContent()).isEqualTo("MoirAI said: She walks forward.");
    }

    @Test
    public void shouldInjectChronicleSegmentsIntoContextWhenVectorSearchReturnsResults() {

        // given
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", AdventureFixture.PUBLIC_ID);

        var savedMessage = MessageFixture.assistantMessage().build();
        ReflectionTestUtils.setField(savedMessage, "publicId", UUID.randomUUID());

        var generationResult = TextGenerationResult.builder()
                .outputText("AI response")
                .build();

        var segmentId = UUID.randomUUID();
        var segment = ChronicleSegment.builder()
                .adventureId(AdventureFixture.NUMERIC_ID)
                .content("The dragon was defeated.")
                .build();

        var command = new SendMessage(UUID.randomUUID(), "Hello!", "user");

        when(adventureRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);
        when(messageRepository.findActiveByAdventureId(anyLong(), anyInt())).thenReturn(List.of());
        when(embeddingPort.embed(anyString())).thenReturn(new float[] { 0.1f, 0.2f });
        when(vectorSearchPort.search(any(UUID.class), any(float[].class), anyInt())).thenReturn(List.of());
        when(chronicleVectorSearchPort.search(any(UUID.class), any(float[].class), anyInt())).thenReturn(List.of(segmentId));
        when(chronicleSegmentRepository.getAllByIds(List.of(segmentId))).thenReturn(List.of(segment));
        when(textCompletionPort.generateTextFrom(any())).thenReturn(generationResult);

        var captor = ArgumentCaptor.forClass(TextGenerationRequest.class);

        // when
        handler.handle(command);

        // then
        verify(textCompletionPort, times(2)).generateTextFrom(captor.capture());
        assertThat(captor.getAllValues().get(1).messages()).anySatisfy(m -> assertThat(m.content()).isEqualTo("The dragon was defeated."));
    }

    @Test
    public void shouldNotInjectChronicleSegmentsWhenVectorSearchReturnsEmpty() {

        // given
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", AdventureFixture.PUBLIC_ID);

        var savedMessage = MessageFixture.assistantMessage().build();
        ReflectionTestUtils.setField(savedMessage, "publicId", UUID.randomUUID());

        var generationResult = TextGenerationResult.builder()
                .outputText("AI response")
                .build();

        var command = new SendMessage(UUID.randomUUID(), "Hello!", "user");

        when(adventureRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);
        when(messageRepository.findActiveByAdventureId(anyLong(), anyInt())).thenReturn(List.of());
        when(embeddingPort.embed(anyString())).thenReturn(new float[] { 0.1f, 0.2f });
        when(vectorSearchPort.search(any(UUID.class), any(float[].class), anyInt())).thenReturn(List.of());
        when(chronicleVectorSearchPort.search(any(UUID.class), any(float[].class), anyInt())).thenReturn(List.of());
        when(textCompletionPort.generateTextFrom(any())).thenReturn(generationResult);

        // when
        var result = handler.handle(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo("AI response");
    }

    @Test
    public void shouldStripTrailingFragmentFromAiResponseBeforeSaving() {

        // given
        var adventure = AdventureFixture.privateSingleplayerAdventure()
                .narrator("MoirAI", "I am a Discord chatbot")
                .build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", AdventureFixture.PUBLIC_ID);

        var savedMessage = MessageFixture.assistantMessage().build();
        ReflectionTestUtils.setField(savedMessage, "publicId", UUID.randomUUID());

        var generationResult = TextGenerationResult.builder()
                .outputText("She walks forward. And then she")
                .build();

        var command = new SendMessage(UUID.randomUUID(), "Hello!", "user");

        when(adventureRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);
        when(messageRepository.findActiveByAdventureId(anyLong(), anyInt())).thenReturn(List.of());
        when(embeddingPort.embed(anyString())).thenReturn(new float[] { 0.1f, 0.2f });
        when(vectorSearchPort.search(any(UUID.class), any(float[].class), anyInt())).thenReturn(List.of());
        when(textCompletionPort.generateTextFrom(any())).thenReturn(generationResult);

        var captor = ArgumentCaptor.forClass(Message.class);

        // when
        handler.handle(command);

        // then
        verify(messageRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues().get(1).getContent()).isEqualTo("MoirAI said: She walks forward.");
    }

    @Test
    public void shouldOmitPersonalityInGenerationRequestWhenNarratorIsNull() {

        // given
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", AdventureFixture.PUBLIC_ID);

        var savedMessage = MessageFixture.assistantMessage().build();
        ReflectionTestUtils.setField(savedMessage, "publicId", UUID.randomUUID());

        var generationResult = TextGenerationResult.builder()
                .outputText("AI response")
                .build();

        var command = new SendMessage(UUID.randomUUID(), "Hello!", "user");

        when(adventureRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);
        when(messageRepository.findActiveByAdventureId(anyLong(), anyInt())).thenReturn(List.of());
        when(embeddingPort.embed(anyString())).thenReturn(new float[] { 0.1f, 0.2f });
        when(vectorSearchPort.search(any(UUID.class), any(float[].class), anyInt())).thenReturn(List.of());
        when(textCompletionPort.generateTextFrom(any())).thenReturn(generationResult);

        var captor = ArgumentCaptor.forClass(TextGenerationRequest.class);

        // when
        handler.handle(command);

        // then
        verify(textCompletionPort, times(2)).generateTextFrom(captor.capture());
        assertThat(captor.getAllValues().get(1).instructions()).isNull();
    }
}
