package me.moirai.storyengine.core.application.command.notification;

import java.util.Optional;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.enums.NotificationStatus;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.port.inbound.notification.NotificationDetails;
import me.moirai.storyengine.core.port.inbound.notification.UpdateNotification;
import me.moirai.storyengine.core.port.outbound.notification.NotificationRepository;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

@CommandHandler
public class UpdateNotificationHandler extends AbstractCommandHandler<UpdateNotification, NotificationDetails> {

    private static final String NOTIFICATION_NOT_FOUND = "Notification to be updated was not found";
    private static final String ID_REQUIRED = "Notification ID cannot be null";

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public UpdateNotificationHandler(
            NotificationRepository notificationRepository,
            UserRepository userRepository) {

        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void validate(UpdateNotification command) {

        if (command.notificationId() == null) {
            throw new IllegalArgumentException(ID_REQUIRED);
        }
    }

    @Override
    public NotificationDetails execute(UpdateNotification command) {

        var notification = notificationRepository.findByPublicId(command.notificationId())
                .orElseThrow(() -> new NotFoundException(NOTIFICATION_NOT_FOUND));

        notification.updateMessage(command.message());
        notification.updateLevel(command.level());

        var saved = notificationRepository.save(notification);
        var user = Optional.ofNullable(notification.getTargetUserId())
                .flatMap(userRepository::findById)
                .orElse(null);

        NotificationStatus status = null;
        String targetUsername = null;
        if (user != null) {
            status = notification.getStatus(user.getId());
            targetUsername = user.getUsername();
        }

        return new NotificationDetails(
                saved.getPublicId(),
                saved.getMessage(),
                saved.getType(),
                saved.getLevel(),
                status,
                targetUsername,
                null,
                saved.isInteractable(),
                saved.getMetadata(),
                saved.getCreationDate(),
                saved.getLastUpdateDate());
    }
}
