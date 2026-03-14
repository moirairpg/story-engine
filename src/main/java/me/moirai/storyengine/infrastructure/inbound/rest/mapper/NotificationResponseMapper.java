package me.moirai.storyengine.infrastructure.inbound.rest.mapper;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import me.moirai.storyengine.core.application.usecase.notification.result.NotificationReadResult;
import me.moirai.storyengine.core.application.usecase.notification.result.NotificationResult;
import me.moirai.storyengine.core.application.usecase.notification.result.SearchNotificationsResult;
import me.moirai.storyengine.infrastructure.inbound.rest.response.NotificationReadResponse;
import me.moirai.storyengine.infrastructure.inbound.rest.response.NotificationResponse;
import me.moirai.storyengine.infrastructure.inbound.rest.response.SearchNotificationsResponse;

@Component
public class NotificationResponseMapper {

    public SearchNotificationsResponse toResponse(SearchNotificationsResult result) {

        List<NotificationResponse> personas = CollectionUtils.emptyIfNull(result.getResults())
                .stream()
                .map(this::toResponse)
                .toList();

        return SearchNotificationsResponse.builder()
                .page(result.getPage())
                .resultsInPage(result.getItems())
                .totalPages(result.getTotalPages())
                .totalResults(result.getTotalItems())
                .results(personas)
                .build();
    }

    public NotificationResponse toResponse(NotificationResult result) {

        return NotificationResponse.builder()
                .message(result.getMessage())
                .receiverDiscordId(result.getReceiverDiscordId())
                .senderDiscordId(result.getSenderDiscordId())
                .type(result.getType())
                .metadata(result.getMetadata())
                .isGlobal(result.isGlobal())
                .isInteractable(result.isInteractable())
                .notificationsRead(result.getNotificationsRead().stream()
                        .map(this::toResponse)
                        .toList())
                .build();
    }

    public NotificationReadResponse toResponse(NotificationReadResult result) {

        return NotificationReadResponse.builder()
                .readAt(result.getReadAt())
                .userId(result.getUserId())
                .build();
    }
}
