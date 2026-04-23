package me.moirai.storyengine.infrastructure.inbound.websocket;

import java.util.UUID;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import me.moirai.storyengine.common.cqs.command.CommandRunner;
import me.moirai.storyengine.core.port.inbound.message.SendMessage;

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
            @Payload WebSocketMessageRequest request) {

        var result = commandRunner.run(new SendMessage(adventureId, request.content()));
        messagingTemplate.convertAndSend("/topic/adventures/" + adventureId, result);
    }
}
