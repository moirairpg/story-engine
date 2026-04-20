package me.moirai.storyengine.infrastructure.inbound.websocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import me.moirai.storyengine.common.security.authentication.MoiraiPrincipal;

public class StompChannelInterceptorTest {

    private final StompChannelInterceptor interceptor = new StompChannelInterceptor();

    @Test
    void shouldReturnMessageUnmodifiedWhenConnectFrameHasPrincipal() {

        // given
        var principal = new MoiraiPrincipal(UUID.randomUUID(), 1L, "discordId", "user",
                "user@test.com", "token", "refresh", null, null);
        var auth = new UsernamePasswordAuthenticationToken(principal, null);
        var accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setUser(auth);
        var message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        // when
        var result = interceptor.preSend(message, null);

        // then
        assertThat(result).isSameAs(message);
    }

    @Test
    void shouldThrowBadCredentialsExceptionWhenConnectFrameHasNoPrincipal() {

        // given
        var accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        var message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        // when / then
        assertThatThrownBy(() -> interceptor.preSend(message, null))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Unauthenticated WebSocket connection");
    }

    @Test
    void shouldReturnMessageUnmodifiedWhenFrameIsNotConnect() {

        // given
        var accessor = StompHeaderAccessor.create(StompCommand.SEND);
        var message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        // when
        var result = interceptor.preSend(message, null);

        // then
        assertThat(result).isSameAs(message);
    }
}
