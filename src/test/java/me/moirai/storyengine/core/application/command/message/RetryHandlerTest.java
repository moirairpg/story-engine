package me.moirai.storyengine.core.application.command.message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import me.moirai.storyengine.common.enums.AiRole;
import me.moirai.storyengine.common.exception.BusinessRuleViolationException;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.domain.chronicle.MessageWindowOverflowEvent;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.message.Message;
import me.moirai.storyengine.core.domain.message.MessageFixture;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.port.inbound.message.Retry;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.chronicle.ChronicleSegmentRepository;
import me.moirai.storyengine.core.port.outbound.generation.EmbeddingPort;
import me.moirai.storyengine.core.port.outbound.generation.TextCompletionPort;
import me.moirai.storyengine.core.port.outbound.generation.TextGenerationResult;
import me.moirai.storyengine.core.port.outbound.message.MessageRepository;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;
import me.moirai.storyengine.core.port.outbound.vectorsearch.ChronicleVectorSearchPort;
import me.moirai.storyengine.core.port.outbound.vectorsearch.LorebookVectorSearchPort;

@ExtendWith(MockitoExtension.class)
public class RetryHandlerTest {

    @Mock
    private AdventureRepository adventureRepository;

    @Mock
    private PersonaRepository personaRepository;

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

    private RetryHandler handler;

    @BeforeEach
    void setup() {
        handler = new RetryHandler(
                adventureRepository,
                personaRepository,
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
    public void shouldThrowWhenAdventureIdIsNull() {

        // given
        var command = new Retry(null);

        // when / then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void shouldThrowWhenAdventureNotFound() {

        // given
        var command = new Retry(UUID.randomUUID());
        when(adventureRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

        // when / then
        assertThrows(NotFoundException.class, () -> handler.handle(command));
    }

    @Test
    public void shouldThrowWhenNoMessagesExist() {

        // given
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", AdventureFixture.PUBLIC_ID);
        ReflectionTestUtils.setField(adventure, "personaId", PersonaFixture.NUMERIC_ID);

        var persona = PersonaFixture.publicPersona().build();
        var command = new Retry(UUID.randomUUID());

        when(adventureRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(personaRepository.findById(anyLong())).thenReturn(Optional.of(persona));
        when(messageRepository.getLastActive(anyLong())).thenReturn(Optional.empty());

        // when / then
        assertThrows(BusinessRuleViolationException.class, () -> handler.handle(command));
    }

    @Test
    public void shouldThrowWhenLastMessageIsNotAssistant() {

        // given
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", AdventureFixture.PUBLIC_ID);
        ReflectionTestUtils.setField(adventure, "personaId", PersonaFixture.NUMERIC_ID);

        var persona = PersonaFixture.publicPersona().build();
        var userMessage = MessageFixture.userMessage().build();
        var command = new Retry(UUID.randomUUID());

        when(adventureRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(personaRepository.findById(anyLong())).thenReturn(Optional.of(persona));
        when(messageRepository.getLastActive(anyLong())).thenReturn(Optional.of(userMessage));

        // when / then
        assertThrows(BusinessRuleViolationException.class, () -> handler.handle(command));
    }

    @Test
    public void shouldDeleteLastAssistantMessageBeforeGenerating() {

        // given
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", AdventureFixture.PUBLIC_ID);
        ReflectionTestUtils.setField(adventure, "personaId", PersonaFixture.NUMERIC_ID);

        var persona = PersonaFixture.publicPersona().build();
        var lastMessage = MessageFixture.assistantMessage().build();
        var savedMessage = MessageFixture.assistantMessage().build();
        ReflectionTestUtils.setField(savedMessage, "publicId", UUID.randomUUID());

        var generationResult = TextGenerationResult.builder().outputText("New AI response.").build();
        var command = new Retry(UUID.randomUUID());

        when(adventureRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(personaRepository.findById(anyLong())).thenReturn(Optional.of(persona));
        when(messageRepository.getLastActive(anyLong()))
                .thenReturn(Optional.of(lastMessage))
                .thenReturn(Optional.of(lastMessage));
        when(messageRepository.findActiveByAdventureId(anyLong(), anyInt())).thenReturn(List.of());
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);
        when(embeddingPort.embed(anyString())).thenReturn(new float[] { 0.1f, 0.2f });
        when(vectorSearchPort.search(any(UUID.class), any(float[].class), anyInt())).thenReturn(List.of());
        when(textCompletionPort.generateTextFrom(any())).thenReturn(generationResult);

        InOrder inOrder = Mockito.inOrder(messageRepository, textCompletionPort);

        // when
        handler.handle(command);

        // then
        inOrder.verify(messageRepository).deleteLastAssistantMessage(anyLong());
        inOrder.verify(textCompletionPort).generateTextFrom(any());
    }

    @Test
    public void shouldReturnAiMessageResult() {

        // given
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", AdventureFixture.PUBLIC_ID);
        ReflectionTestUtils.setField(adventure, "personaId", PersonaFixture.NUMERIC_ID);

        var persona = PersonaFixture.publicPersona().build();
        var lastMessage = MessageFixture.assistantMessage().build();
        var savedMessage = MessageFixture.assistantMessage().build();
        ReflectionTestUtils.setField(savedMessage, "publicId", UUID.randomUUID());

        var generationResult = TextGenerationResult.builder().outputText("Retried AI response.").build();
        var command = new Retry(UUID.randomUUID());

        when(adventureRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(personaRepository.findById(anyLong())).thenReturn(Optional.of(persona));
        when(messageRepository.getLastActive(anyLong()))
                .thenReturn(Optional.of(lastMessage))
                .thenReturn(Optional.of(lastMessage));
        when(messageRepository.findActiveByAdventureId(anyLong(), anyInt())).thenReturn(List.of());
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);
        when(embeddingPort.embed(anyString())).thenReturn(new float[] { 0.1f, 0.2f });
        when(vectorSearchPort.search(any(UUID.class), any(float[].class), anyInt())).thenReturn(List.of());
        when(textCompletionPort.generateTextFrom(any())).thenReturn(generationResult);

        // when
        var result = handler.handle(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo("Retried AI response.");
        assertThat(result.role()).isEqualTo(AiRole.ASSISTANT);
    }

    @Test
    public void shouldPublishOverflowEvent() {

        // given
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", AdventureFixture.PUBLIC_ID);
        ReflectionTestUtils.setField(adventure, "personaId", PersonaFixture.NUMERIC_ID);

        var persona = PersonaFixture.publicPersona().build();
        var lastMessage = MessageFixture.assistantMessage().build();
        var savedMessage = MessageFixture.assistantMessage().build();
        ReflectionTestUtils.setField(savedMessage, "publicId", UUID.randomUUID());

        var generationResult = TextGenerationResult.builder().outputText("Retried AI response.").build();
        var command = new Retry(UUID.randomUUID());

        when(adventureRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(personaRepository.findById(anyLong())).thenReturn(Optional.of(persona));
        when(messageRepository.getLastActive(anyLong()))
                .thenReturn(Optional.of(lastMessage))
                .thenReturn(Optional.of(lastMessage));
        when(messageRepository.findActiveByAdventureId(anyLong(), anyInt())).thenReturn(List.of());
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);
        when(embeddingPort.embed(anyString())).thenReturn(new float[] { 0.1f, 0.2f });
        when(vectorSearchPort.search(any(UUID.class), any(float[].class), anyInt())).thenReturn(List.of());
        when(textCompletionPort.generateTextFrom(any())).thenReturn(generationResult);

        // when
        handler.handle(command);

        // then
        verify(eventPublisher).publishEvent(any(MessageWindowOverflowEvent.class));
    }
}
