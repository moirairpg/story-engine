package me.moirai.storyengine.core.application.command.notification;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.port.inbound.notification.DeleteNotification;
import me.moirai.storyengine.core.port.outbound.notification.NotificationRepository;

@CommandHandler
public class DeleteNotificationHandler extends AbstractCommandHandler<DeleteNotification, Void> {

    private static final String NOTIFICATION_NOT_FOUND = "Notification to be deleted was not found";
    private static final String ID_REQUIRED = "Notification ID cannot be null";

    private final NotificationRepository notificationRepository;

    public DeleteNotificationHandler(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void validate(DeleteNotification command) {

        if (command.notificationId() == null) {
            throw new IllegalArgumentException(ID_REQUIRED);
        }
    }

    @Override
    public Void execute(DeleteNotification command) {

        notificationRepository.findByPublicId(command.notificationId())
                .orElseThrow(() -> new NotFoundException(NOTIFICATION_NOT_FOUND));

        notificationRepository.deleteByPublicId(command.notificationId());

        return null;
    }
}
