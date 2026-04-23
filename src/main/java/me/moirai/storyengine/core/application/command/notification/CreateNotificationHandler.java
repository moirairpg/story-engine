package me.moirai.storyengine.core.application.command.notification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.enums.NotificationStatus;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.common.util.Functions;
import me.moirai.storyengine.core.domain.notification.Notification;
import me.moirai.storyengine.core.domain.notification.NotificationCreated;
import me.moirai.storyengine.core.domain.notification.NotificationType;
import me.moirai.storyengine.core.domain.userdetails.User;
import me.moirai.storyengine.core.port.inbound.notification.CreateNotification;
import me.moirai.storyengine.core.port.inbound.notification.NotificationDetails;
import me.moirai.storyengine.core.port.outbound.notification.NotificationRepository;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

@CommandHandler
public class CreateNotificationHandler extends AbstractCommandHandler<CreateNotification, List<NotificationDetails>> {

    private static final String MESSAGE_REQUIRED = "Notification message cannot be null or empty";
    private static final String SYSTEM_REQUIRES_TARGET = "SYSTEM notifications must have at least one target user";
    private static final String BROADCAST_REQUIRES_NO_TARGET = "BROADCAST notifications cannot have target users";
    private static final String GAME_NOT_ALLOWED = "GAME notifications cannot be created via this command";
    private static final String UNKNOWN_USERS = "Unknown usernames: ";

    private final NotificationRepository notificationRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final UserRepository userRepository;

    public CreateNotificationHandler(
            NotificationRepository notificationRepository,
            ApplicationEventPublisher eventPublisher,
            UserRepository userRepository) {

        this.notificationRepository = notificationRepository;
        this.eventPublisher = eventPublisher;
        this.userRepository = userRepository;
    }

    @Override
    public void validate(CreateNotification command) {

        if (command.message() == null || command.message().isBlank()) {
            throw new IllegalArgumentException(MESSAGE_REQUIRED);
        }

        if (command.type() == NotificationType.GAME) {
            throw new IllegalArgumentException(GAME_NOT_ALLOWED);
        }

        if (command.type() == NotificationType.SYSTEM
                && (command.targetUsernames() == null || command.targetUsernames().isEmpty())) {

            throw new IllegalArgumentException(SYSTEM_REQUIRES_TARGET);
        }

        if (command.type() == NotificationType.BROADCAST
                && (command.targetUsernames() != null && !command.targetUsernames().isEmpty())) {

            throw new IllegalArgumentException(BROADCAST_REQUIRES_NO_TARGET);
        }
    }

    @Override
    public List<NotificationDetails> execute(CreateNotification command) {

        if (command.type() == NotificationType.BROADCAST) {
            var notification = createNotification(command, null);
            return List.of(toResult(null, notification));
        }

        var resolvedUsers = resolveUsers(command.targetUsernames());
        return resolvedUsers.stream()
                .map(user -> {
                    var notification = createNotification(command, user);
                    return toResult(user, notification);
                })
                .toList();
    }

    private NotificationDetails toResult(
            User user,
            Notification notification) {

        return new NotificationDetails(
                notification.getPublicId(),
                notification.getMessage(),
                notification.getType(),
                notification.getLevel(),
                NotificationStatus.UNREAD,
                Functions.mapOrNull(user, User::getUsername),
                null,
                notification.isInteractable(),
                notification.getMetadata(),
                notification.getCreationDate(),
                notification.getLastUpdateDate());
    }

    private List<User> resolveUsers(List<String> usernames) {

        var resolved = new ArrayList<User>();
        var missing = new ArrayList<String>();

        for (var username : usernames) {
            userRepository.findByUsername(username)
                    .ifPresentOrElse(resolved::add, () -> missing.add(username));
        }

        if (!missing.isEmpty()) {
            throw new NotFoundException(UNKNOWN_USERS + String.join(", ", missing));
        }

        return resolved;
    }

    private Notification createNotification(
            CreateNotification command,
            User user) {

        var notification = notificationRepository.save(Notification.builder()
                .message(command.message())
                .type(command.type())
                .level(command.level())
                .isInteractable(command.isInteractable())
                .metadata(command.metadata())
                .targetUserId(Functions.mapOrNull(user, User::getId))
                .build());

        eventPublisher.publishEvent(new NotificationCreated(notification.getPublicId()));

        return notification;
    }
}
