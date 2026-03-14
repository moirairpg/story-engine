package me.moirai.storyengine.infrastructure.outbound.adapter.mapper;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import me.moirai.storyengine.core.port.inbound.notification.NotificationDetails;
import me.moirai.storyengine.core.port.inbound.notification.SearchNotificationsResult;
import me.moirai.storyengine.core.domain.notification.Notification;

@Component
public class NotificationPersistenceMapper {

    public NotificationDetails mapToResult(Notification notification) {

        return NotificationDetails.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .receiverDiscordId(notification.getReceiverDiscordId())
                .senderDiscordId(notification.getSenderDiscordId())
                .type(notification.getType().name())
                .metadata(notification.getMetadata())
                .isGlobal(notification.isGlobal())
                .isInteractable(notification.isInteractable())
                .creationDate(notification.getCreationDate())
                .lastUpdateDate(notification.getLastUpdateDate())
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
