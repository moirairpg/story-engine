package me.moirai.storyengine.infrastructure.inbound.websocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import me.moirai.storyengine.common.cqs.command.CommandRunner;
import me.moirai.storyengine.common.enums.AiRole;
import me.moirai.storyengine.core.port.inbound.message.MessageResult;
import me.moirai.storyengine.core.port.inbound.message.SendMessage;

@ExtendWith(MockitoExtension.class)
public class AdventureWebSocketControllerTest {

    @Mock
    private CommandRunner commandRunner;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private AdventureWebSocketController controller;

    @Test
    void shouldRunCommandAndSendResultWhenPayloadIsValid() {

        // given
        var adventureId = UUID.randomUUID();
        var request = new WebSocketMessageRequest("hello");
        var result = new MessageResult(UUID.randomUUID(), "reply", AiRole.ASSISTANT, Instant.now());

        when(commandRunner.run(any(SendMessage.class))).thenReturn(result);

        // when
        controller.handleMessage(adventureId, request);

        // then
        var commandCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(commandRunner).run(commandCaptor.capture());
        assertThat(commandCaptor.getValue().adventureId()).isEqualTo(adventureId);
        assertThat(commandCaptor.getValue().content()).isEqualTo("hello");
        verify(messagingTemplate).convertAndSend(eq("/topic/adventures/" + adventureId), eq(result));
    }
}
