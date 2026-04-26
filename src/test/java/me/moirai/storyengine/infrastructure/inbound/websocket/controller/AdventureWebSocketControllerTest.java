package me.moirai.storyengine.infrastructure.inbound.websocket.controller;

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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import me.moirai.storyengine.common.cqs.command.CommandRunner;
import me.moirai.storyengine.common.enums.MessageAuthorRole;
import me.moirai.storyengine.common.security.authentication.MoiraiPrincipal;
import me.moirai.storyengine.core.port.inbound.message.MessageResult;
import me.moirai.storyengine.core.port.inbound.message.SendMessage;
import me.moirai.storyengine.infrastructure.inbound.websocket.request.WebSocketMessageRequest;

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
        var result = new MessageResult(UUID.randomUUID(), "reply", MessageAuthorRole.ASSISTANT, Instant.now());
        var moiraiPrincipal = new MoiraiPrincipal(UUID.randomUUID(), 99999L, "discordId",
                "alice", "alice@test.com", "token", "refresh", null, null);
        var principal = new UsernamePasswordAuthenticationToken(moiraiPrincipal, null);

        when(commandRunner.run(any(SendMessage.class))).thenReturn(result);

        // when
        controller.handleMessage(adventureId, request, principal);

        // then
        var commandCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(commandRunner).run(commandCaptor.capture());
        assertThat(commandCaptor.getValue().adventureId()).isEqualTo(adventureId);
        assertThat(commandCaptor.getValue().content()).isEqualTo("hello");
        assertThat(commandCaptor.getValue().username()).isEqualTo("alice");
        verify(messagingTemplate).convertAndSend(eq("/topic/adventures/" + adventureId), eq(result));
    }
}
