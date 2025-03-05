package me.moirai.discordbot.infrastructure.inbound.socket.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import me.moirai.discordbot.common.usecases.UseCaseRunner;
import me.moirai.discordbot.common.web.SecurityContextAware;
import me.moirai.discordbot.core.application.usecase.notification.request.SendNotification;
import me.moirai.discordbot.core.application.usecase.notification.request.StreamNotificationsForUser;
import me.moirai.discordbot.core.application.usecase.notification.result.NotificationResult;
import me.moirai.discordbot.core.application.usecase.notification.result.SendNotificationResult;
import me.moirai.discordbot.infrastructure.inbound.socket.request.SendNotificationRequest;
import me.moirai.discordbot.infrastructure.inbound.socket.response.NotificationResponse;
import me.moirai.discordbot.infrastructure.inbound.socket.response.SendNotificationResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
public class NotificationSocketController extends SecurityContextAware {

    private final UseCaseRunner useCaseRunner;

    public NotificationSocketController(UseCaseRunner useCaseRunner) {
        this.useCaseRunner = useCaseRunner;
    }

    @MessageMapping("notifications.stream")
    public Flux<NotificationResponse> subscribeToNotifications(@Payload String receiverDiscordId) {

        StreamNotificationsForUser query = StreamNotificationsForUser.create(receiverDiscordId);
        return useCaseRunner.run(query).map(this::toResponse);
    }

    @MessageMapping("notifications.send")
    public Mono<SendNotificationResponse> sendNotification(@Payload SendNotificationRequest request) {

        SendNotification command = SendNotification.builder()
                .isGlobal(request.getIsGlobal())
                .isInteractable(request.getIsInteractable())
                .message(request.getMessage())
                .metadata(request.getMetadata())
                .receiverDiscordId(request.getReceiverDiscordId())
                .senderDiscordId(request.getSenderDiscordId())
                .type(request.getType())
                .build();

        return Mono.just(useCaseRunner.run(command))
                .map(this::toResponse);
    }

    private SendNotificationResponse toResponse(SendNotificationResult result) {
        return SendNotificationResponse.withIdAndCreationDateTime(result.getId(), result.getCreationDateTime());
    }

    private NotificationResponse toResponse(NotificationResult result) {

        return NotificationResponse.builder()
                .isGlobal(result.isGlobal())
                .isInteractable(result.isInteractable())
                .message(result.getMessage())
                .metadata(result.getMetadata())
                .receiverDiscordId(result.getReceiverDiscordId())
                .senderDiscordId(result.getSenderDiscordId())
                .type(result.getType())
                .build();
    }
}
