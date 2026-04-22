package me.moirai.storyengine.core.application.command.notification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.enums.NotificationStatus;
import me.moirai.storyengine.common.exception.BusinessRuleViolationException;
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
    }

    @Override
    public List<NotificationDetails> execute(CreateNotification command) {

        if (command.type() == NotificationType.BROADCAST) {
            return List.of(createOne(command, null));
        }

        var resolvedUsers = resolveUsers(command.targetUsernames());

        var results = new ArrayList<NotificationDetails>();
        for (var user : resolvedUsers) {
            results.add(createOne(command, user));
        }

        return List.copyOf(results);
    }

    private List<User> resolveUsers(List<String> usernames) {

        var resolved = new ArrayList<User>();
        var missing = new ArrayList<String>();

        for (var username : usernames) {
            userRepository.findByUsername(username)
                    .ifPresentOrElse(resolved::add, () -> missing.add(username));
        }

        if (!missing.isEmpty()) {
            throw new BusinessRuleViolationException(UNKNOWN_USERS + String.join(", ", missing));
        }

        return resolved;
    }

    private NotificationDetails createOne(CreateNotification command, User targetUser) {

        var notificationBuilder = Notification.builder()
                .message(command.message())
                .type(command.type())
                .level(command.level())
                .adventureId(command.adventureId())
                .isInteractable(command.isInteractable())
                .metadata(command.metadata());

        if (targetUser != null) {
            notificationBuilder.targetUserId(targetUser.getId());
        }

        var notification = notificationRepository.save(notificationBuilder.build());

        eventPublisher.publishEvent(new NotificationCreated(notification.getPublicId()));

        return mapResult(notification);
    }

    private NotificationDetails mapResult(Notification notification) {
        return new NotificationDetails(
                notification.getPublicId(),
                notification.getMessage(),
                notification.getType(),
                notification.getLevel(),
                NotificationStatus.UNREAD,
                notification.getTargetUserId(),
                notification.getAdventureId(),
                notification.isInteractable(),
                notification.getMetadata(),
                notification.getCreationDate(),
                notification.getLastUpdateDate());
    }
}
