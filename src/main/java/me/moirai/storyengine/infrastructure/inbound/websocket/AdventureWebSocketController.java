package me.moirai.storyengine.infrastructure.inbound.websocket;

import java.security.Principal;
import java.util.UUID;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import me.moirai.storyengine.common.cqs.command.CommandRunner;
import me.moirai.storyengine.common.security.authentication.MoiraiPrincipal;
import me.moirai.storyengine.common.security.authentication.MoiraiSecurityContext;
import me.moirai.storyengine.core.port.inbound.message.SendMessage;
import tools.jackson.databind.json.JsonMapper;

@Controller
public class AdventureWebSocketController {

    private final CommandRunner commandRunner;
    private final JsonMapper jsonMapper;
    private final SimpMessagingTemplate messagingTemplate;

    public AdventureWebSocketController(
            CommandRunner commandRunner,
            JsonMapper jsonMapper,
            SimpMessagingTemplate messagingTemplate) {

        this.commandRunner = commandRunner;
        this.jsonMapper = jsonMapper;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/adventure/{adventureId}")
    public void handleMessage(
            @DestinationVariable UUID adventureId,
            @Payload String rawPayload,
            Principal principal) throws Exception {

        var auth = (UsernamePasswordAuthenticationToken) principal;
        try {
            MoiraiSecurityContext.set((MoiraiPrincipal) auth.getPrincipal());
            var request = jsonMapper.readValue(rawPayload, WebSocketMessageRequest.class);
            var command = new SendMessage(adventureId, request.content());
            var result = commandRunner.run(command);
            messagingTemplate.convertAndSend("/topic/adventure/" + adventureId,
                    jsonMapper.writeValueAsString(result));
        } finally {
            MoiraiSecurityContext.clear();
        }
    }
}
