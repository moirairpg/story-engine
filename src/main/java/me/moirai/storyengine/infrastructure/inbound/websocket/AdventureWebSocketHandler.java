package me.moirai.storyengine.infrastructure.inbound.websocket;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import me.moirai.storyengine.common.cqs.command.CommandRunner;
import me.moirai.storyengine.common.security.authentication.MoiraiPrincipal;
import me.moirai.storyengine.common.security.authentication.MoiraiSecurityContext;
import me.moirai.storyengine.core.port.inbound.message.SendMessage;
import tools.jackson.databind.json.JsonMapper;

@Component
public class AdventureWebSocketHandler extends TextWebSocketHandler {

    private static final String ADVENTURE_ID_ATTR = "adventureId";

    private final CommandRunner commandRunner;
    private final JsonMapper objectMapper;

    public AdventureWebSocketHandler(
            CommandRunner commandRunner,
            JsonMapper objectMapper) {

        this.commandRunner = commandRunner;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        var path = session.getUri().getPath();
        var adventureId = UUID.fromString(path.substring(path.lastIndexOf('/') + 1));
        session.getAttributes().put(ADVENTURE_ID_ATTR, adventureId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        var auth = (Authentication) session.getPrincipal();
        MoiraiSecurityContext.set((MoiraiPrincipal) auth.getPrincipal());
        try {
            var request = objectMapper.readValue(message.getPayload(), WebSocketMessageRequest.class);
            var adventureId = (UUID) session.getAttributes().get(ADVENTURE_ID_ATTR);
            var command = new SendMessage(adventureId, request.content());
            var result = commandRunner.run(command);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(result)));
        } finally {
            MoiraiSecurityContext.clear();
        }
    }
}
