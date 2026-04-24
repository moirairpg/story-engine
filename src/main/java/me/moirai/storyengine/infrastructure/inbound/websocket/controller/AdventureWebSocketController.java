package me.moirai.storyengine.infrastructure.inbound.websocket.controller;

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
import me.moirai.storyengine.infrastructure.inbound.websocket.request.WebSocketMessageRequest;

@Controller
public class AdventureWebSocketController {

    private final CommandRunner commandRunner;
    private final SimpMessagingTemplate messagingTemplate;

    public AdventureWebSocketController(
            CommandRunner commandRunner,
            SimpMessagingTemplate messagingTemplate) {

        this.commandRunner = commandRunner;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/adventures/{adventureId}")
    public void handleMessage(
            @DestinationVariable UUID adventureId,
            @Payload WebSocketMessageRequest request,
            Principal principal) {

        var auth = (UsernamePasswordAuthenticationToken) principal;
        var moiraiPrincipal = (MoiraiPrincipal) auth.getPrincipal();

        try {
            MoiraiSecurityContext.set(moiraiPrincipal);
            var result = commandRunner.run(new SendMessage(adventureId, request.content(), moiraiPrincipal.username()));
            messagingTemplate.convertAndSend("/topic/adventures/" + adventureId, result);
        } finally {
            MoiraiSecurityContext.clear();
        }
    }
}
