package me.moirai.storyengine.core.application.command.message;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.core.port.inbound.message.DeleteMessage;
import me.moirai.storyengine.core.port.outbound.message.MessageRepository;

@ExtendWith(MockitoExtension.class)
public class DeleteMessageHandlerTest {

    @Mock
    private MessageRepository messageRepository;

    private DeleteMessageHandler handler;

    @BeforeEach
    void setup() {
        handler = new DeleteMessageHandler(messageRepository);
    }

    @Test
    public void shouldThrowWhenAdventureIdIsNull() {

        // given
        var command = new DeleteMessage(null, UUID.randomUUID());

        // when / then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void shouldThrowWhenMessageIdIsNull() {

        // given
        var command = new DeleteMessage(UUID.randomUUID(), null);

        // when / then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void shouldDeleteMessageByPublicId() {

        // given
        var adventureId = UUID.randomUUID();
        var messageId = UUID.randomUUID();
        var command = new DeleteMessage(adventureId, messageId);

        // when
        handler.handle(command);

        // then
        verify(messageRepository).deleteByPublicId(adventureId, messageId);
    }
}
