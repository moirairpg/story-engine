package me.moirai.storyengine.infrastructure.inbound.websocket.interceptor;

import java.security.Principal;
import java.util.Set;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ExecutorChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import me.moirai.storyengine.common.exception.AuthenticationFailedException;
import me.moirai.storyengine.common.security.authentication.MoiraiPrincipal;
import me.moirai.storyengine.common.security.authentication.MoiraiSecurityContext;
import me.moirai.storyengine.common.security.authentication.MoiraiUserDetailsService;

@Component
public class StompChannelInterceptor implements ExecutorChannelInterceptor {

    private static final String UNAUTHENTICATED = "Unauthenticated WebSocket connection";
    private static final String TOKEN_CLUSTER = "%s / %s";
    private static final Set<StompCommand> REAUTHENTICATED_COMMANDS = Set.of(StompCommand.SEND, StompCommand.SUBSCRIBE);

    private final MoiraiUserDetailsService userDetailsService;

    public StompChannelInterceptor(MoiraiUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        var accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        if (accessor.getCommand() == StompCommand.CONNECT && accessor.getUser() == null) {
            throw new BadCredentialsException(UNAUTHENTICATED);
        }

        if (REAUTHENTICATED_COMMANDS.contains(accessor.getCommand())) {
            reauthenticate(accessor.getUser());
        }

        return message;
    }

    private void reauthenticate(Principal user) {

        var principal = (MoiraiPrincipal) ((Authentication) user).getPrincipal();
        var tokenCluster = String.format(TOKEN_CLUSTER, principal.authorizationToken(), principal.refreshToken());

        try {
            userDetailsService.loadUserByUsername(tokenCluster);
        } catch (AuthenticationFailedException e) {
            throw new BadCredentialsException(UNAUTHENTICATED, e);
        }
    }

    @Override
    public Message<?> beforeHandle(Message<?> message, MessageChannel channel, MessageHandler handler) {
        var user = SimpMessageHeaderAccessor.getUser(message.getHeaders());

        if (user instanceof Authentication authentication
                && authentication.getPrincipal() instanceof MoiraiPrincipal principal) {

            MoiraiSecurityContext.set(principal);
        }

        return message;
    }

    @Override
    public void afterMessageHandled(
            Message<?> message, MessageChannel channel, MessageHandler handler, Exception exception) {

        MoiraiSecurityContext.clear();
    }
}
