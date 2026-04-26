package me.moirai.storyengine.core.application.command.message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import me.moirai.storyengine.common.enums.MessageAuthorRole;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.message.Message;
import me.moirai.storyengine.core.domain.message.MessageFixture;
import me.moirai.storyengine.core.port.inbound.message.Say;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.message.MessageRepository;

@ExtendWith(MockitoExtension.class)
public class SayHandlerTest {

    @Mock
    private AdventureRepository adventureRepository;

    @Mock
    private MessageRepository messageRepository;

    private SayHandler handler;

    @BeforeEach
    void setup() {
        handler = new SayHandler(adventureRepository, messageRepository);
    }

    @Test
    public void shouldThrowWhenAdventureIdIsNull() {

        // given
        var command = new Say(null, "Some content");

        // when / then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void shouldThrowWhenContentIsBlank() {

        // given
        var command = new Say(UUID.randomUUID(), "");

        // when / then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void shouldThrowWhenAdventureNotFound() {

        // given
        var command = new Say(UUID.randomUUID(), "Some content");
        when(adventureRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

        // when / then
        assertThrows(NotFoundException.class, () -> handler.handle(command));
    }

    @Test
    public void shouldPersistContentAsAssistantMessage() {

        // given
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", AdventureFixture.PUBLIC_ID);

        var savedMessage = MessageFixture.assistantMessage().build();
        ReflectionTestUtils.setField(savedMessage, "publicId", UUID.randomUUID());

        var command = new Say(UUID.randomUUID(), "Hello world");

        when(adventureRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);

        var captor = ArgumentCaptor.forClass(Message.class);

        // when
        handler.handle(command);

        // then
        verify(messageRepository).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(MessageAuthorRole.ASSISTANT);
    }

    @Test
    public void shouldReturnMessageResult() {

        // given
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", AdventureFixture.PUBLIC_ID);

        var savedMessage = MessageFixture.assistantMessage().build();
        ReflectionTestUtils.setField(savedMessage, "publicId", UUID.randomUUID());

        var command = new Say(UUID.randomUUID(), "Hello world");

        when(adventureRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);

        // when
        var result = handler.handle(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo("Hello world");
        assertThat(result.role()).isEqualTo(MessageAuthorRole.ASSISTANT);
    }
}
