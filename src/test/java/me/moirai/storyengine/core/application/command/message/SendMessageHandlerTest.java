package me.moirai.storyengine.core.application.command.message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.message.Message;
import me.moirai.storyengine.core.domain.message.MessageFixture;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureLorebookEntryDetails;
import me.moirai.storyengine.core.port.inbound.message.MessageResult;
import me.moirai.storyengine.core.port.inbound.message.SendMessage;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureLorebookReader;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.generation.EmbeddingPort;
import me.moirai.storyengine.core.port.outbound.generation.TextCompletionPort;
import me.moirai.storyengine.core.port.outbound.generation.TextGenerationResult;
import me.moirai.storyengine.core.port.outbound.message.MessageReader;
import me.moirai.storyengine.core.port.outbound.message.MessageRepository;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;
import me.moirai.storyengine.core.port.outbound.vectorsearch.VectorSearchPort;

@ExtendWith(MockitoExtension.class)
public class SendMessageHandlerTest {

    @Mock
    private AdventureRepository adventureRepository;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private MessageReader messageReader;

    @Mock
    private TextCompletionPort textCompletionPort;

    @Mock
    private EmbeddingPort embeddingPort;

    @Mock
    private VectorSearchPort vectorSearchPort;

    @Mock
    private AdventureLorebookReader lorebookReader;

    private SendMessageHandler handler;

    @BeforeEach
    void setup() {
        handler = new SendMessageHandler(
                adventureRepository,
                personaRepository,
                messageRepository,
                messageReader,
                textCompletionPort,
                embeddingPort,
                vectorSearchPort,
                lorebookReader,
                10,
                5);
    }

    @Test
    public void shouldThrowWhenAdventureNotFound() {

        // Given
        var command = new SendMessage(UUID.randomUUID(), "Hello!");
        when(adventureRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

        // When / Then
        assertThrows(NotFoundException.class, () -> handler.handle(command));
    }

    @Test
    public void shouldThrowWhenPersonaNotFound() {

        // Given
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "personaId", PersonaFixture.NUMERIC_ID);

        var command = new SendMessage(UUID.randomUUID(), "Hello!");

        when(adventureRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(personaRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When / Then
        assertThrows(NotFoundException.class, () -> handler.handle(command));
    }

    @Test
    public void shouldSavePlayerMessageBeforeGeneratingResponse() {

        // Given
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "personaId", PersonaFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", AdventureFixture.PUBLIC_ID);

        var persona = PersonaFixture.publicPersona().build();

        var savedMessage = MessageFixture.assistantMessage().build();
        ReflectionTestUtils.setField(savedMessage, "publicId", UUID.randomUUID());

        var generationResult = TextGenerationResult.builder()
                .outputText("AI response")
                .build();

        var command = new SendMessage(UUID.randomUUID(), "Hello!");

        when(adventureRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(personaRepository.findById(anyLong())).thenReturn(Optional.of(persona));
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);
        when(messageReader.findActiveByAdventureId(anyLong(), anyInt())).thenReturn(List.of());
        when(embeddingPort.embed(anyString())).thenReturn(new float[] { 0.1f, 0.2f });
        when(vectorSearchPort.search(any(UUID.class), any(float[].class), anyInt())).thenReturn(List.of());
        when(textCompletionPort.generateTextFrom(any())).thenReturn(generationResult);

        // When
        MessageResult result = handler.handle(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo("AI response");
    }

    @Test
    public void shouldSaveAiMessageAfterGeneratingResponse() {

        // Given
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "personaId", PersonaFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", AdventureFixture.PUBLIC_ID);

        var persona = PersonaFixture.publicPersona().build();

        var savedMessage = MessageFixture.assistantMessage().build();
        ReflectionTestUtils.setField(savedMessage, "publicId", UUID.randomUUID());

        var generationResult = TextGenerationResult.builder()
                .outputText("AI response")
                .build();

        var command = new SendMessage(UUID.randomUUID(), "Hello!");

        when(adventureRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(personaRepository.findById(anyLong())).thenReturn(Optional.of(persona));
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);
        when(messageReader.findActiveByAdventureId(anyLong(), anyInt())).thenReturn(List.of());
        when(embeddingPort.embed(anyString())).thenReturn(new float[] { 0.1f, 0.2f });
        when(vectorSearchPort.search(any(UUID.class), any(float[].class), anyInt())).thenReturn(List.of());
        when(textCompletionPort.generateTextFrom(any())).thenReturn(generationResult);

        // When
        MessageResult result = handler.handle(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.role()).isEqualTo(savedMessage.getRole());
    }

    @Test
    public void shouldPrependLorebookEntriesWhenVectorSearchReturnsResults() {

        // Given
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "personaId", PersonaFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", AdventureFixture.PUBLIC_ID);

        var persona = PersonaFixture.publicPersona().build();

        var savedMessage = MessageFixture.assistantMessage().build();
        ReflectionTestUtils.setField(savedMessage, "publicId", UUID.randomUUID());

        var generationResult = TextGenerationResult.builder()
                .outputText("AI response")
                .build();

        var entryId = UUID.randomUUID();
        var lorebookEntry = new AdventureLorebookEntryDetails(
                entryId,
                adventure.getPublicId(),
                "Dragon",
                "A fearsome dragon",
                null,
                false,
                null,
                null);

        var command = new SendMessage(UUID.randomUUID(), "Hello!");

        when(adventureRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(personaRepository.findById(anyLong())).thenReturn(Optional.of(persona));
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);
        when(messageReader.findActiveByAdventureId(anyLong(), anyInt())).thenReturn(List.of());
        when(embeddingPort.embed(anyString())).thenReturn(new float[] { 0.1f, 0.2f });
        when(vectorSearchPort.search(any(UUID.class), any(float[].class), anyInt())).thenReturn(List.of(entryId));
        when(lorebookReader.getAllByIds(List.of(entryId))).thenReturn(List.of(lorebookEntry));
        when(textCompletionPort.generateTextFrom(any())).thenReturn(generationResult);

        // When
        var result = handler.handle(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo("AI response");
    }

    @Test
    public void shouldNotPrependLorebookEntriesWhenVectorSearchReturnsEmptyList() {

        // Given
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "personaId", PersonaFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", AdventureFixture.PUBLIC_ID);

        var persona = PersonaFixture.publicPersona().build();

        var savedMessage = MessageFixture.assistantMessage().build();
        ReflectionTestUtils.setField(savedMessage, "publicId", UUID.randomUUID());

        var generationResult = TextGenerationResult.builder()
                .outputText("AI response")
                .build();

        var command = new SendMessage(UUID.randomUUID(), "Hello!");

        when(adventureRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(personaRepository.findById(anyLong())).thenReturn(Optional.of(persona));
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);
        when(messageReader.findActiveByAdventureId(anyLong(), anyInt())).thenReturn(List.of());
        when(embeddingPort.embed(anyString())).thenReturn(new float[] { 0.1f, 0.2f });
        when(vectorSearchPort.search(any(UUID.class), any(float[].class), anyInt())).thenReturn(List.of());
        when(textCompletionPort.generateTextFrom(any())).thenReturn(generationResult);

        // When
        MessageResult result = handler.handle(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo("AI response");
    }

    @Test
    public void shouldNotPrependLorebookEntriesWhenLorebookReaderReturnsEmptyList() {

        // Given
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "personaId", PersonaFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", AdventureFixture.PUBLIC_ID);

        var persona = PersonaFixture.publicPersona().build();

        var savedMessage = MessageFixture.assistantMessage().build();
        ReflectionTestUtils.setField(savedMessage, "publicId", UUID.randomUUID());

        var generationResult = TextGenerationResult.builder()
                .outputText("AI response")
                .build();

        var entryId = UUID.randomUUID();
        var command = new SendMessage(UUID.randomUUID(), "Hello!");

        when(adventureRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(personaRepository.findById(anyLong())).thenReturn(Optional.of(persona));
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);
        when(messageReader.findActiveByAdventureId(anyLong(), anyInt())).thenReturn(List.of());
        when(embeddingPort.embed(anyString())).thenReturn(new float[] { 0.1f, 0.2f });
        when(vectorSearchPort.search(any(UUID.class), any(float[].class), anyInt())).thenReturn(List.of(entryId));
        when(lorebookReader.getAllByIds(List.of(entryId))).thenReturn(List.of());
        when(textCompletionPort.generateTextFrom(any())).thenReturn(generationResult);

        // When
        MessageResult result = handler.handle(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo("AI response");
    }

    @Test
    public void shouldThrowExceptionWhenAdventureIdIsNull() {

        // Given
        var command = new SendMessage(null, "Hello!");

        // When / Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void shouldThrowExceptionWhenContentIsBlank() {

        // Given
        var command = new SendMessage(UUID.randomUUID(), "");

        // When / Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }
}
