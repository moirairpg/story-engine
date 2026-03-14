package me.moirai.storyengine.infrastructure.inbound.rest.controller;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.text.CaseUtils.toCamelCase;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import me.moirai.storyengine.common.usecases.UseCaseRunner;
import me.moirai.storyengine.common.web.SecurityContextAware;
import me.moirai.storyengine.core.port.inbound.notification.GetNotificationById;
import me.moirai.storyengine.core.port.inbound.notification.NotificationReadResult;
import me.moirai.storyengine.core.port.inbound.notification.NotificationResult;
import me.moirai.storyengine.core.port.inbound.notification.ReadNotification;
import me.moirai.storyengine.core.port.inbound.notification.SearchNotifications;
import me.moirai.storyengine.core.port.inbound.notification.SearchNotificationsResult;
import me.moirai.storyengine.infrastructure.inbound.rest.request.NotificationSearchParameters;
import me.moirai.storyengine.infrastructure.inbound.rest.request.enums.SearchDirection;
import me.moirai.storyengine.infrastructure.inbound.rest.request.enums.SearchNotificationSortingField;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/notification")
@Tag(name = "Notifications", description = "Endpoints for managing MoirAI Notifications")
public class NotificationController extends SecurityContextAware {

    private final UseCaseRunner useCaseRunner;

    public NotificationController(UseCaseRunner useCaseRunner) {
        this.useCaseRunner = useCaseRunner;
    }

    @GetMapping
    @ResponseStatus(code = HttpStatus.OK)
    @PreAuthorize("isAdmin()")
    public Mono<SearchNotificationsResult> search(NotificationSearchParameters searchParameters) {

        return mapWithAuthenticatedUser(authenticatedUser -> {

            SearchNotifications.Builder requestBuilder = SearchNotifications.builder()
                    .receiverDiscordId(searchParameters.getReceiverDiscordId())
                    .senderDiscordId(searchParameters.getSenderDiscordId())
                    .type(searchParameters.getType())
                    .sortingField(getSortingField(searchParameters.getSortingField()))
                    .direction(getDirection(searchParameters.getDirection()));

            if (isNotBlank(searchParameters.getGlobal())) {
                requestBuilder.isGlobal(Boolean.valueOf(searchParameters.getGlobal()));
            }

            if (isNotBlank(searchParameters.getInteractable())) {
                requestBuilder.isGlobal(Boolean.valueOf(searchParameters.getInteractable()));
            }

            return useCaseRunner.run(requestBuilder.build());
        });
    }

    @GetMapping("/{notificationId}")
    @ResponseStatus(code = HttpStatus.OK)
    @PreAuthorize("isAdmin()")
    public Mono<NotificationResult> getNotificationById(@PathVariable(required = true) String notificationId) {

        return mapWithAuthenticatedUser(authenticatedUser -> {

            GetNotificationById request = GetNotificationById.create(notificationId);
            return useCaseRunner.run(request);
        });
    }

    @PatchMapping("/{notificationId}")
    @ResponseStatus(code = HttpStatus.OK)
    public Mono<NotificationReadResult> readNotification(@PathVariable(required = true) String notificationId) {

        return mapWithAuthenticatedUser(authenticatedUser -> {

            ReadNotification request = ReadNotification.create(notificationId, notificationId);
            return useCaseRunner.run(request);
        });
    }

    private String getSortingField(SearchNotificationSortingField searchSortingField) {

        if (searchSortingField != null) {
            return toCamelCase(searchSortingField.name(), false, '_');
        }

        return EMPTY;
    }

    private String getDirection(SearchDirection searchDirection) {

        if (searchDirection != null) {
            return toCamelCase(searchDirection.name(), false, '_');
        }

        return EMPTY;
    }
}
