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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import me.moirai.storyengine.common.enums.AiRole;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.common.security.authentication.MoiraiPrincipal;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.message.Message;
import me.moirai.storyengine.core.domain.message.MessageFixture;
import me.moirai.storyengine.core.domain.message.MessageStatus;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureLorebookEntryDetails;
import me.moirai.storyengine.core.port.inbound.message.SendMessage;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureLorebookReader;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.generation.EmbeddingPort;
import me.moirai.storyengine.core.port.outbound.generation.TextCompletionPort;
import me.moirai.storyengine.core.port.outbound.generation.TextGenerationRequest;
import me.moirai.storyengine.core.port.outbound.generation.TextGenerationResult;
import me.moirai.storyengine.core.port.outbound.message.MessageData;
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
    void setupSecurityContext() {

        var principal = new MoiraiPrincipal(UUID.randomUUID(), 99999L, "discordId",
                "user", "user@test.com", "token", "refresh", null, null);
        var authentication = new UsernamePasswordAuthenticationToken(principal, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

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

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void shouldThrowWhenAdventureNotFound() {

        // given
        var command = new SendMessage(UUID.randomUUID(), "Hello!");
        when(adventureRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

        // when / then
        assertThrows(NotFoundException.class, () -> handler.handle(command));
    }

    @Test
    public void shouldThrowWhenPersonaNotFound() {

        // given
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "personaId", PersonaFixture.NUMERIC_ID);

        var command = new SendMessage(UUID.randomUUID(), "Hello!");

        when(adventureRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(personaRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when / then
        assertThrows(NotFoundException.class, () -> handler.handle(command));
    }

    @Test
    public void shouldSavePlayerMessageBeforeGeneratingResponse() {

        // given
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

        // when
        var result = handler.handle(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo("AI response");
    }

    @Test
    public void shouldNotPrependLorebookEntriesWhenLorebookReaderReturnsEmptyList() {

        // given
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

        // when
        var result = handler.handle(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo("AI response");
    }

    @Test
    public void shouldThrowExceptionWhenAdventureIdIsNull() {

        // given
        var command = new SendMessage(null, "Hello!");

        // when / then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void shouldThrowExceptionWhenContentIsBlank() {

        // given
        var command = new SendMessage(UUID.randomUUID(), "");

        // when / then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void shouldSavePlayerMessageWithCharacterNamePrefix() {

        // given
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "personaId", PersonaFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", AdventureFixture.PUBLIC_ID);

        adventure.addLorebookEntry("Aldric", "A brave warrior", "user");

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
        ReflectionTestUtils.setField(adventure, "personaId", PersonaFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", AdventureFixture.PUBLIC_ID);

        var persona = PersonaFixture.publicPersona().build();

        var savedMessage = MessageFixture.assistantMessage().build();
        ReflectionTestUtils.setField(savedMessage, "publicId", UUID.randomUUID());

        var generationResult = TextGenerationResult.builder()
                .outputText("AI response")
                .build();

        var userMessageData = new MessageData(UUID.randomUUID(), AdventureFixture.NUMERIC_ID, "user",
                AiRole.USER, "Aldric said: I look around.", null, MessageStatus.ACTIVE);
        var assistantMessageData = new MessageData(UUID.randomUUID(), AdventureFixture.NUMERIC_ID, "system",
                AiRole.ASSISTANT, "MoirAI said: You see a tavern.", null, MessageStatus.ACTIVE);

        var command = new SendMessage(UUID.randomUUID(), "Hello!");

        when(adventureRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(personaRepository.findById(anyLong())).thenReturn(Optional.of(persona));
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);
        when(messageReader.findActiveByAdventureId(anyLong(), anyInt()))
                .thenReturn(List.of(userMessageData, assistantMessageData));
        when(embeddingPort.embed(anyString())).thenReturn(new float[] { 0.1f, 0.2f });
        when(vectorSearchPort.search(any(UUID.class), any(float[].class), anyInt())).thenReturn(List.of());
        when(textCompletionPort.generateTextFrom(any())).thenReturn(generationResult);

        var captor = ArgumentCaptor.forClass(TextGenerationRequest.class);

        // when
        handler.handle(command);

        // then
        verify(textCompletionPort).generateTextFrom(captor.capture());
        var messages = captor.getValue().messages();
        assertThat(messages).anySatisfy(m -> assertThat(m.content()).isEqualTo("Aldric said: I look around."));
        assertThat(messages).anySatisfy(m -> assertThat(m.content()).isEqualTo("MoirAI said: You see a tavern."));
    }

    @Test
    public void shouldReplaceNamePlaceholderInPersonaPersonality() {

        // given
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "personaId", PersonaFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", AdventureFixture.PUBLIC_ID);

        var persona = PersonaFixture.publicPersona().personality("I am {name}, a Discord chatbot").build();

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

        var captor = ArgumentCaptor.forClass(TextGenerationRequest.class);

        // when
        handler.handle(command);

        // then
        verify(textCompletionPort).generateTextFrom(captor.capture());
        assertThat(captor.getValue().instructions()).isEqualTo("I am MoirAI, a Discord chatbot");
    }

    @Test
    public void shouldStripChatPrefixFromAiResponseBeforeSaving() {

        // given
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "personaId", PersonaFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", AdventureFixture.PUBLIC_ID);

        var persona = PersonaFixture.publicPersona().build();

        var savedMessage = MessageFixture.assistantMessage().build();
        ReflectionTestUtils.setField(savedMessage, "publicId", UUID.randomUUID());

        var generationResult = TextGenerationResult.builder()
                .outputText("MoirAI said: some text.")
                .build();

        var command = new SendMessage(UUID.randomUUID(), "Hello!");

        when(adventureRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(personaRepository.findById(anyLong())).thenReturn(Optional.of(persona));
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);
        when(messageReader.findActiveByAdventureId(anyLong(), anyInt())).thenReturn(List.of());
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
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "personaId", PersonaFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", AdventureFixture.PUBLIC_ID);

        var persona = PersonaFixture.publicPersona().build();

        var savedMessage = MessageFixture.assistantMessage().build();
        ReflectionTestUtils.setField(savedMessage, "publicId", UUID.randomUUID());

        var generationResult = TextGenerationResult.builder()
                .outputText("As MoirAI, She walks forward.")
                .build();

        var command = new SendMessage(UUID.randomUUID(), "Hello!");

        when(adventureRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(personaRepository.findById(anyLong())).thenReturn(Optional.of(persona));
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);
        when(messageReader.findActiveByAdventureId(anyLong(), anyInt())).thenReturn(List.of());
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
    public void shouldStripTrailingFragmentFromAiResponseBeforeSaving() {

        // given
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "personaId", PersonaFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", AdventureFixture.PUBLIC_ID);

        var persona = PersonaFixture.publicPersona().build();

        var savedMessage = MessageFixture.assistantMessage().build();
        ReflectionTestUtils.setField(savedMessage, "publicId", UUID.randomUUID());

        var generationResult = TextGenerationResult.builder()
                .outputText("She walks forward. And then she")
                .build();

        var command = new SendMessage(UUID.randomUUID(), "Hello!");

        when(adventureRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(personaRepository.findById(anyLong())).thenReturn(Optional.of(persona));
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);
        when(messageReader.findActiveByAdventureId(anyLong(), anyInt())).thenReturn(List.of());
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
}
