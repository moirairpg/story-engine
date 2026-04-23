package me.moirai.storyengine.core.application.command.notification;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.enums.NotificationStatus;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.port.inbound.notification.ReadNotification;
import me.moirai.storyengine.core.port.outbound.notification.NotificationRepository;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

@CommandHandler
public class ReadNotificationHandler extends AbstractCommandHandler<ReadNotification, Void> {

    private static final String NOTIFICATION_NOT_FOUND = "Notification to be read was not found";
    private static final String ID_REQUIRED = "Notification ID cannot be null";

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public ReadNotificationHandler(
            NotificationRepository notificationRepository,
            UserRepository userRepository) {

        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void validate(ReadNotification command) {

        if (command.notificationId() == null) {
            throw new IllegalArgumentException(ID_REQUIRED);
        }
    }

    @Override
    public Void execute(ReadNotification command) {

        var notification = notificationRepository.findByPublicId(command.notificationId())
                .orElseThrow(() -> new NotFoundException(NOTIFICATION_NOT_FOUND));

        var user = userRepository.findByUsername(command.username())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (notification.getStatus(user.getId()) == NotificationStatus.UNREAD) {
            notification.markAsRead(user.getId());
            notificationRepository.save(notification);
        }

        return null;
    }
}
