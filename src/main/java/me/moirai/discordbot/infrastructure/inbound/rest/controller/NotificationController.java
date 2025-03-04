package me.moirai.discordbot.infrastructure.inbound.rest.controller;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.text.CaseUtils.toCamelCase;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import me.moirai.discordbot.common.usecases.UseCaseRunner;
import me.moirai.discordbot.common.web.SecurityContextAware;
import me.moirai.discordbot.core.application.usecase.notification.request.GetNotificationById;
import me.moirai.discordbot.core.application.usecase.notification.request.SearchNotifications;
import me.moirai.discordbot.infrastructure.inbound.rest.mapper.NotificationResponseMapper;
import me.moirai.discordbot.infrastructure.inbound.rest.request.NotificationSearchParameters;
import me.moirai.discordbot.infrastructure.inbound.rest.request.enums.SearchDirection;
import me.moirai.discordbot.infrastructure.inbound.rest.request.enums.SearchNotificationSortingField;
import me.moirai.discordbot.infrastructure.inbound.rest.response.NotificationResponse;
import me.moirai.discordbot.infrastructure.inbound.rest.response.SearchNotificationsResponse;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/notification")
@Tag(name = "Notifications", description = "Endpoints for managing MoirAI Notifications")
public class NotificationController extends SecurityContextAware {

    private final UseCaseRunner useCaseRunner;
    private final NotificationResponseMapper responseMapper;

    public NotificationController(UseCaseRunner useCaseRunner,
            NotificationResponseMapper responseMapper) {

        this.useCaseRunner = useCaseRunner;
        this.responseMapper = responseMapper;
    }

    @GetMapping
    @ResponseStatus(code = HttpStatus.OK)
    @PreAuthorize("isAdmin()")
    public Mono<SearchNotificationsResponse> search(NotificationSearchParameters searchParameters) {

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

            return responseMapper.toResponse(useCaseRunner.run(requestBuilder.build()));
        });
    }

    @GetMapping("/{notificationId}")
    @ResponseStatus(code = HttpStatus.OK)
    @PreAuthorize("isAdmin()")
    public Mono<NotificationResponse> getNotificationById(@PathVariable(required = true) String notificationId) {

        return mapWithAuthenticatedUser(authenticatedUser -> {

            GetNotificationById request = GetNotificationById.create(notificationId);
            return responseMapper.toResponse(useCaseRunner.run(request));
        });
    }

    // TODO implement read endpoint

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
