package me.moirai.storyengine.infrastructure.inbound.websocket.interceptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.security.Principal;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
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
    void tearDown() {
        MoiraiSecurityContext.clear();
    }

    @Test
    void shouldAcceptConnectFrameWhenPrincipalPresent() {

        // given
        var principal = new MoiraiPrincipal(UUID.randomUUID(), 1L, "user",
                "token", "refresh", null, null);
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

    @Test
    void shouldExposeTheAuthenticatedUserWhenBeforeHandleReceivesAnAuthenticatedFrame() {

        // given
        var principal = principal();
        var message = messageFrom(new UsernamePasswordAuthenticationToken(principal, null));

        // when
        interceptor.beforeHandle(message, null, null);

        // then
        assertThat(MoiraiSecurityContext.getAuthenticatedUser()).isSameAs(principal);
    }

    @Test
    void shouldReturnTheMessageUnmodifiedWhenBeforeHandleRuns() {

        // given
        var message = messageFrom(new UsernamePasswordAuthenticationToken(principal(), null));

        // when
        var result = interceptor.beforeHandle(message, null, null);

        // then
        assertThat(result).isSameAs(message);
    }

    @Test
    void shouldLeaveTheSecurityContextEmptyWhenTheFrameHasNoUser() {

        // given
        var message = messageFrom(null);

        // when
        interceptor.beforeHandle(message, null, null);

        // then
        assertThat(MoiraiSecurityContext.getAuthenticatedUser()).isNull();
    }

    @Test
    void shouldLeaveTheSecurityContextEmptyWhenTheUserIsNotAMoiraiPrincipal() {

        // given
        var message = messageFrom(new UsernamePasswordAuthenticationToken("alice", null));

        // when
        interceptor.beforeHandle(message, null, null);

        // then
        assertThat(MoiraiSecurityContext.getAuthenticatedUser()).isNull();
    }

    @Test
    void shouldNotPopulateTheSecurityContextWhenPreSendRuns() {

        // given
        var message = messageFrom(new UsernamePasswordAuthenticationToken(principal(), null));

        // when
        interceptor.preSend(message, null);

        // then
        assertThat(MoiraiSecurityContext.getAuthenticatedUser()).isNull();
    }

    @Test
    void shouldClearTheSecurityContextWhenTheMessageHasBeenHandled() {

        // given
        var message = messageFrom(new UsernamePasswordAuthenticationToken(principal(), null));
        interceptor.beforeHandle(message, null, null);

        // when
        interceptor.afterMessageHandled(message, null, null, null);

        // then
        assertThat(MoiraiSecurityContext.getAuthenticatedUser()).isNull();
    }

    @Test
    void shouldClearTheSecurityContextWhenTheHandlerThrew() {

        // given
        var message = messageFrom(new UsernamePasswordAuthenticationToken(principal(), null));
        interceptor.beforeHandle(message, null, null);

        // when
        interceptor.afterMessageHandled(message, null, null, new IllegalStateException("boom"));

        // then
        assertThat(MoiraiSecurityContext.getAuthenticatedUser()).isNull();
    }

    private Message<byte[]> messageFrom(Principal user) {

        var accessor = StompHeaderAccessor.create(StompCommand.SEND);
        accessor.setUser(user);

        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private MoiraiPrincipal principal() {
        return new MoiraiPrincipal(UUID.randomUUID(), 99999L,
                "alice", "token", "refresh", null, null);
    }
}
