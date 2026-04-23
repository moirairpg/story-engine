package me.moirai.storyengine.infrastructure.inbound.websocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import me.moirai.storyengine.common.security.authentication.MoiraiPrincipal;
import me.moirai.storyengine.common.security.authentication.MoiraiSecurityContext;

public class StompChannelInterceptorTest {

    private final StompChannelInterceptor interceptor = new StompChannelInterceptor();

    @AfterEach
    void clearContext() {
        MoiraiSecurityContext.clear();
    }

    @Test
    void shouldPopulateSecurityContextWhenConnectFrameHasPrincipal() {

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
        assertThat(MoiraiSecurityContext.getAuthenticatedUser()).isSameAs(principal);
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
        assertThat(MoiraiSecurityContext.getAuthenticatedUser()).isNull();
    }

    @Test
    void shouldPopulateSecurityContextWhenNonConnectFrameHasPrincipal() {

        // given
        var principal = new MoiraiPrincipal(UUID.randomUUID(), 2L, "discordId", "user",
                "user@test.com", "token", "refresh", null, null);
        var auth = new UsernamePasswordAuthenticationToken(principal, null);
        var accessor = StompHeaderAccessor.create(StompCommand.SEND);
        accessor.setUser(auth);
        var message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        // when
        var result = interceptor.preSend(message, null);

        // then
        assertThat(result).isSameAs(message);
        assertThat(MoiraiSecurityContext.getAuthenticatedUser()).isSameAs(principal);
    }

    @Test
    void shouldReturnMessageUnmodifiedWhenFrameIsNotConnectAndHasNoPrincipal() {

        // given
        var accessor = StompHeaderAccessor.create(StompCommand.SEND);
        var message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        // when
        var result = interceptor.preSend(message, null);

        // then
        assertThat(result).isSameAs(message);
        assertThat(MoiraiSecurityContext.getAuthenticatedUser()).isNull();
    }

    @Test
    void shouldClearSecurityContextOnAfterSendCompletion() {

        // given
        var principal = new MoiraiPrincipal(UUID.randomUUID(), 3L, "discordId", "user",
                "user@test.com", "token", "refresh", null, null);
        MoiraiSecurityContext.set(principal);
        var accessor = StompHeaderAccessor.create(StompCommand.SEND);
        var message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        // when
        interceptor.afterSendCompletion(message, null, true, null);

        // then
        assertThat(MoiraiSecurityContext.getAuthenticatedUser()).isNull();
    }

    @Test
    void shouldClearSecurityContextOnAfterSendCompletionWhenExceptionOccurred() {

        // given
        var principal = new MoiraiPrincipal(UUID.randomUUID(), 4L, "discordId", "user",
                "user@test.com", "token", "refresh", null, null);
        MoiraiSecurityContext.set(principal);
        var accessor = StompHeaderAccessor.create(StompCommand.SEND);
        var message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        // when
        interceptor.afterSendCompletion(message, null, false, new RuntimeException("boom"));

        // then
        assertThat(MoiraiSecurityContext.getAuthenticatedUser()).isNull();
    }
}
