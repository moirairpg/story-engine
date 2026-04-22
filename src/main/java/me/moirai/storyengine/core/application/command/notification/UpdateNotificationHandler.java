package me.moirai.storyengine.core.application.command.notification;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.port.inbound.notification.NotificationDetails;
import me.moirai.storyengine.core.port.inbound.notification.UpdateNotification;
import me.moirai.storyengine.core.port.outbound.notification.NotificationRepository;

@CommandHandler
public class UpdateNotificationHandler extends AbstractCommandHandler<UpdateNotification, NotificationDetails> {

    private static final String NOTIFICATION_NOT_FOUND = "Notification to be updated was not found";
    private static final String ID_REQUIRED = "Notification ID cannot be null";

    private final NotificationRepository notificationRepository;

    public UpdateNotificationHandler(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
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

        return new NotificationDetails(
                saved.getPublicId(),
                saved.getMessage(),
                saved.getType(),
                saved.getLevel(),
                saved.getStatus(command.requesterId()),
                saved.getTargetUserId(),
                saved.getAdventureId(),
                saved.isInteractable(),
                saved.getMetadata(),
                saved.getCreationDate(),
                saved.getLastUpdateDate());
    }
}
