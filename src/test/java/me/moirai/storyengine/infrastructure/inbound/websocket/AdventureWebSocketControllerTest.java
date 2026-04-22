package me.moirai.storyengine.infrastructure.inbound.websocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import me.moirai.storyengine.common.cqs.command.CommandRunner;
import me.moirai.storyengine.common.enums.AiRole;
import me.moirai.storyengine.common.security.authentication.MoiraiPrincipal;
import me.moirai.storyengine.common.security.authentication.MoiraiSecurityContext;
import me.moirai.storyengine.core.port.inbound.message.MessageResult;
import me.moirai.storyengine.core.port.inbound.message.SendMessage;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
public class AdventureWebSocketControllerTest {

    @Mock
    private CommandRunner commandRunner;

    @Mock
    private JsonMapper jsonMapper;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private AdventureWebSocketController controller;

    @AfterEach
    void clearSecurityContext() {
        MoiraiSecurityContext.clear();
    }

    @Test
    void shouldRunCommandAndSendResultWhenPayloadIsValid() throws Exception {

        // given
        var adventureId = UUID.randomUUID();
        var rawPayload = "{\"content\":\"hello\"}";
        var request = new WebSocketMessageRequest("hello");
        var result = new MessageResult(UUID.randomUUID(), "reply", AiRole.ASSISTANT, Instant.now());
        var serializedResult = "{\"id\":\"x\"}";
        var principal = new MoiraiPrincipal(UUID.randomUUID(), 1L, "discordId", "user",
                "user@test.com", "token", "refresh", null, null);
        var auth = new UsernamePasswordAuthenticationToken(principal, null);

        when(jsonMapper.readValue(rawPayload, WebSocketMessageRequest.class)).thenReturn(request);
        when(commandRunner.run(any(SendMessage.class))).thenReturn(result);
        when(jsonMapper.writeValueAsString(result)).thenReturn(serializedResult);

        // when
        controller.handleMessage(adventureId, rawPayload, auth);

        // then
        var commandCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(commandRunner).run(commandCaptor.capture());
        assertThat(commandCaptor.getValue().adventureId()).isEqualTo(adventureId);
        assertThat(commandCaptor.getValue().content()).isEqualTo("hello");
        verify(messagingTemplate).convertAndSend(eq("/topic/adventure/" + adventureId), eq(serializedResult));
        assertThat(MoiraiSecurityContext.getAuthenticatedUser()).isNull();
    }

    @Test
    void shouldClearSecurityContextWhenCommandThrows() throws Exception {

        // given
        var adventureId = UUID.randomUUID();
        var rawPayload = "{\"content\":\"boom\"}";
        var request = new WebSocketMessageRequest("boom");
        var principal = new MoiraiPrincipal(UUID.randomUUID(), 2L, "discordId", "user",
                "user@test.com", "token", "refresh", null, null);
        var auth = new UsernamePasswordAuthenticationToken(principal, null);

        when(jsonMapper.readValue(rawPayload, WebSocketMessageRequest.class)).thenReturn(request);
        when(commandRunner.run(any(SendMessage.class))).thenThrow(new RuntimeException("kaboom"));

        // when / then
        assertThatThrownBy(() -> controller.handleMessage(adventureId, rawPayload, auth))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("kaboom");
        assertThat(MoiraiSecurityContext.getAuthenticatedUser()).isNull();
    }
}
