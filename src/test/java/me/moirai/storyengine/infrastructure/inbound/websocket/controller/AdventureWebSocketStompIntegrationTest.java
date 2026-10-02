package me.moirai.storyengine.infrastructure.inbound.websocket.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.JacksonJsonMessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import me.moirai.storyengine.AbstractDatabaseIntegrationTest;
import me.moirai.storyengine.common.cqs.command.CommandRunner;
import me.moirai.storyengine.common.enums.Role;
import me.moirai.storyengine.common.exception.AuthenticationFailedException;
import me.moirai.storyengine.common.security.authentication.MoiraiCookie;
import me.moirai.storyengine.common.security.authentication.MoiraiPrincipal;
import me.moirai.storyengine.common.security.authentication.MoiraiSecurityContext;
import me.moirai.storyengine.common.security.authentication.MoiraiUserDetailsService;
import me.moirai.storyengine.core.port.inbound.message.SendMessage;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class AdventureWebSocketStompIntegrationTest extends AbstractDatabaseIntegrationTest {

    private static final UUID ADVENTURE_ID = UUID.fromString("00000000-0000-0000-0000-0000000000aa");
    private static final String SESSION_TOKEN = "valid-session-token";
    private static final long TIMEOUT_SECONDS = 10;

    @LocalServerPort
    private int port;

    @MockitoBean
    private MoiraiUserDetailsService userDetailsService;

    @MockitoBean
    private CommandRunner commandRunner;

    private WebSocketStompClient stompClient;
    private StompSession session;

    @BeforeEach
    void setUp() {
        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new JacksonJsonMessageConverter());
    }

    @AfterEach
    void tearDown() {

        if (session != null && session.isConnected()) {
            session.disconnect();
        }

        stompClient.stop();
    }

    @Test
    void shouldDeliverAFrameFromTheWireToTheHandlerWithTheAuthenticatedUser() throws Exception {

        // given
        when(userDetailsService.loadUserByUsername(any())).thenReturn(principal());

        var received = new CompletableFuture<SendMessage>();
        doAnswer(invocation -> {
            received.complete(invocation.getArgument(0));
            return null;
        }).when(commandRunner).run(any());

        session = connect(SESSION_TOKEN);

        // when
        session.send("/app/adventures/" + ADVENTURE_ID + "/messages", new WebSocketPayload("hello from the wire"));

        // then
        var command = received.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertThat(command.adventureId()).isEqualTo(ADVENTURE_ID);
        assertThat(command.content()).isEqualTo("hello from the wire");
        assertThat(command.username()).isEqualTo("alice");
        assertThat(command.generateNarration()).isTrue();
    }

    @Test
    void shouldNotRequestNarrationWhenTheFrameSaysSo() throws Exception {

        // given
        when(userDetailsService.loadUserByUsername(any())).thenReturn(principal());

        var received = new CompletableFuture<SendMessage>();
        doAnswer(invocation -> {
            received.complete(invocation.getArgument(0));
            return null;
        }).when(commandRunner).run(any());

        session = connect(SESSION_TOKEN);

        // when
        session.send("/app/adventures/" + ADVENTURE_ID + "/messages",
                new WebSocketPayloadWithNarrationChoice("quiet message", false));

        // then
        var command = received.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertThat(command.content()).isEqualTo("quiet message");
        assertThat(command.generateNarration()).isFalse();
    }

    @Test
    void shouldExposeTheAuthenticatedUserThroughMoiraiSecurityContextWhenTheFrameArrives() throws Exception {

        // given
        when(userDetailsService.loadUserByUsername(any())).thenReturn(principal());

        var seen = new CompletableFuture<MoiraiPrincipal>();
        doAnswer(invocation -> {
            seen.complete(MoiraiSecurityContext.getAuthenticatedUser());
            return null;
        }).when(commandRunner).run(any());

        session = connect(SESSION_TOKEN);

        // when
        session.send("/app/adventures/" + ADVENTURE_ID + "/messages", new WebSocketPayload("hello"));

        // then
        var principalDuringHandling = seen.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertThat(principalDuringHandling).isNotNull();
        assertThat(principalDuringHandling.username()).isEqualTo("alice");
    }

    @Test
    void shouldRejectTheHandshakeWhenNoSessionCookieIsPresent() {

        // then
        assertThatThrownBy(() -> connect(null))
                .isInstanceOf(ExecutionException.class);
    }

    @Test
    void shouldCloseTheSessionWhenTheTokenIsNoLongerValidOnTheNextFrame() throws Exception {

        // given
        when(userDetailsService.loadUserByUsername(any()))
                .thenReturn(principal())
                .thenThrow(new AuthenticationFailedException("Invalid user requested authentication"));

        var connectionLost = new CompletableFuture<Throwable>();

        session = connect(SESSION_TOKEN, new StompSessionHandlerAdapter() {

            @Override
            public void handleTransportError(StompSession stompSession, Throwable exception) {
                connectionLost.complete(exception);
            }
        });

        // when
        session.send("/app/adventures/" + ADVENTURE_ID + "/messages", new WebSocketPayload("hello"));

        // then
        connectionLost.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        assertThat(session.isConnected()).isFalse();
        verify(commandRunner, never()).run(any());
    }

    private StompSession connect(String sessionToken) throws Exception {

        return connect(sessionToken, new StompSessionHandlerAdapter() {
        });
    }

    private StompSession connect(String sessionToken, StompSessionHandlerAdapter sessionHandler) throws Exception {

        var handshakeHeaders = new WebSocketHttpHeaders();

        if (sessionToken != null) {
            handshakeHeaders.add("Cookie", MoiraiCookie.SESSION_COOKIE.getName() + "=" + sessionToken);
        }

        return stompClient
                .connectAsync(
                        "ws://localhost:" + port + "/ws",
                        handshakeHeaders,
                        sessionHandler)
                .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    private MoiraiPrincipal principal() {
        return new MoiraiPrincipal(
                UUID.randomUUID(), 99999L, "alice", "token", "refresh", Role.PLAYER, null);
    }

    record WebSocketPayload(String content) {
    }

    record WebSocketPayloadWithNarrationChoice(String content, boolean generateNarration) {
    }
}
