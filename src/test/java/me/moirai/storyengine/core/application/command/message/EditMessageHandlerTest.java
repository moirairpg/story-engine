package me.moirai.storyengine.core.application.command.message;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.port.inbound.message.EditMessage;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.message.MessageRepository;

@ExtendWith(MockitoExtension.class)
public class EditMessageHandlerTest {

    @Mock
    private AdventureRepository adventureRepository;

    @Mock
    private MessageRepository messageRepository;

    private EditMessageHandler handler;

    @BeforeEach
    void setup() {
        handler = new EditMessageHandler(adventureRepository, messageRepository);
    }

    @Test
    public void shouldThrowWhenAdventureIdIsNull() {

        // given
        var command = new EditMessage(null, UUID.randomUUID(), "Some content", "player1");

        // when / then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void shouldThrowWhenMessageIdIsNull() {

        // given
        var command = new EditMessage(UUID.randomUUID(), null, "Some content", "player1");

        // when / then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void shouldThrowWhenContentIsBlank() {

        // given
        var command = new EditMessage(UUID.randomUUID(), UUID.randomUUID(), "", "player1");

        // when / then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void shouldUpdateContentThenDeleteAfter() {

        // given
        var adventureId = UUID.randomUUID();
        var messageId = UUID.randomUUID();
        var username = "player1";
        var command = new EditMessage(adventureId, messageId, "Updated content", username);
        var adventure = AdventureFixture.privateSingleplayerAdventure().build();
        var ordered = inOrder(messageRepository);

        when(adventureRepository.findByPublicId(adventureId)).thenReturn(Optional.of(adventure));

        // when
        handler.handle(command);

        // then
        ordered.verify(messageRepository).updateContent(eq(adventureId), eq(messageId), any());
        ordered.verify(messageRepository).deleteNewerThanByPublicId(adventureId, messageId);
    }
}
