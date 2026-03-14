package me.moirai.storyengine.infrastructure.outbound.persistence.mapper;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import me.moirai.storyengine.core.application.usecase.notification.result.NotificationResult;
import me.moirai.storyengine.core.application.usecase.notification.result.SearchNotificationsResult;
import me.moirai.storyengine.core.domain.notification.Notification;

@Component
public class NotificationPersistenceMapper {

    public NotificationResult mapToResult(Notification notification) {

        return NotificationResult.builder()
                .message(notification.getMessage())
                .receiverDiscordId(notification.getReceiverDiscordId())
                .senderDiscordId(notification.getSenderDiscordId())
                .type(notification.getType().name())
                .metadata(notification.getMetadata())
                .isGlobal(notification.isGlobal())
                .isInteractable(notification.isInteractable())
                .build();
    }

    public SearchNotificationsResult mapToResult(Page<Notification> pagedResult) {

        return SearchNotificationsResult.builder()
                .results(pagedResult.getContent()
                        .stream()
                        .map(this::mapToResult)
                        .toList())
                .page(pagedResult.getNumber() + 1)
                .items(pagedResult.getNumberOfElements())
                .totalItems(pagedResult.getTotalElements())
                .totalPages(pagedResult.getTotalPages())
                .build();
    }
}
