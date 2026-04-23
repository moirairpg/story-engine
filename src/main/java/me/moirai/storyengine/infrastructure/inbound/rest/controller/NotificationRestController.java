package me.moirai.storyengine.infrastructure.inbound.rest.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import me.moirai.storyengine.common.annotation.Authorize;
import me.moirai.storyengine.common.cqs.command.CommandRunner;
import me.moirai.storyengine.common.cqs.query.QueryRunner;
import me.moirai.storyengine.common.dto.PaginatedResult;
import me.moirai.storyengine.common.enums.NotificationStatus;
import me.moirai.storyengine.common.enums.SortDirection;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.common.web.SecurityContextAware;
import me.moirai.storyengine.core.domain.notification.NotificationLevel;
import me.moirai.storyengine.core.domain.notification.NotificationType;
import me.moirai.storyengine.core.port.inbound.notification.CreateNotification;
import me.moirai.storyengine.core.port.inbound.notification.DeleteNotification;
import me.moirai.storyengine.core.port.inbound.notification.GetNotification;
import me.moirai.storyengine.core.port.inbound.notification.NotificationDetails;
import me.moirai.storyengine.core.port.inbound.notification.NotificationSortField;
import me.moirai.storyengine.core.port.inbound.notification.NotificationSummary;
import me.moirai.storyengine.core.port.inbound.notification.ReadNotification;
import me.moirai.storyengine.core.port.inbound.notification.SearchNotifications;
import me.moirai.storyengine.core.port.inbound.notification.UpdateNotification;
import me.moirai.storyengine.infrastructure.inbound.rest.request.CreateNotificationRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.UpdateNotificationRequest;

@RestController
@RequestMapping("/notifications")
@Tag(name = "Notifications", description = "Endpoints for managing MoirAI Notifications")
public class NotificationRestController extends SecurityContextAware {

    private final QueryRunner queryRunner;
    private final CommandRunner commandRunner;

    public NotificationRestController(
            QueryRunner queryRunner,
            CommandRunner commandRunner) {

        this.queryRunner = queryRunner;
        this.commandRunner = commandRunner;
    }

    @GetMapping("/{notificationId}")
    @ResponseStatus(HttpStatus.OK)
    @Authorize(operation = AuthorizationOperation.VIEW_NOTIFICATION, fields = "#notificationId")
    public NotificationDetails getNotification(@PathVariable UUID notificationId) {

        var user = getAuthenticatedUser();
        return queryRunner.run(new GetNotification(
                notificationId,
                user.username(),
                user.role()));
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Authorize(operation = AuthorizationOperation.MANAGE_NOTIFICATION)
    public PaginatedResult<NotificationSummary> searchNotifications(
            @RequestParam(required = false) NotificationType type,
            @RequestParam(required = false) NotificationLevel level,
            @RequestParam(required = false) NotificationStatus status,
            @RequestParam(name = "receiver_id", required = false) UUID receiverId,
            @RequestParam(name = "sorting_field", required = false) NotificationSortField sortingField,
            @RequestParam(name = "sorting_direction", required = false) SortDirection direction,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        return queryRunner.run(new SearchNotifications(
                type,
                level,
                status,
                receiverId,
                sortingField,
                direction,
                page,
                size));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Authorize(operation = AuthorizationOperation.MANAGE_NOTIFICATION)
    public List<NotificationDetails> createNotification(@RequestBody CreateNotificationRequest request) {
        return commandRunner.run(new CreateNotification(
                request.message(),
                request.type(),
                request.level(),
                request.targetUsernames(),
                request.isInteractable(),
                request.metadata()));
    }

    @PatchMapping("/{notificationId}")
    @ResponseStatus(HttpStatus.OK)
    @Authorize(operation = AuthorizationOperation.MANAGE_NOTIFICATION)
    public NotificationDetails updateNotification(
            @PathVariable UUID notificationId,
            @RequestBody UpdateNotificationRequest request) {

        return commandRunner.run(new UpdateNotification(
                notificationId,
                request.message(),
                request.level()));
    }

    @DeleteMapping("/{notificationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Authorize(operation = AuthorizationOperation.MANAGE_NOTIFICATION)
    public void deleteNotification(@PathVariable UUID notificationId) {
        commandRunner.run(new DeleteNotification(notificationId));
    }

    @PostMapping("/{notificationId}/read")
    @ResponseStatus(HttpStatus.CREATED)
    @Authorize(operation = AuthorizationOperation.VIEW_NOTIFICATION, fields = "#notificationId")
    public void readNotification(@PathVariable UUID notificationId) {

        var user = getAuthenticatedUser();
        commandRunner.run(new ReadNotification(notificationId, user.username()));
    }
}
