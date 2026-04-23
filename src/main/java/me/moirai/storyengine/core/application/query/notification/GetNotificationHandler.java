package me.moirai.storyengine.core.application.query.notification;

import me.moirai.storyengine.common.annotation.QueryHandler;
import me.moirai.storyengine.common.cqs.query.AbstractQueryHandler;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.port.inbound.notification.GetNotification;
import me.moirai.storyengine.core.port.inbound.notification.NotificationDetails;
import me.moirai.storyengine.core.port.outbound.notification.NotificationReader;

@QueryHandler
public class GetNotificationHandler extends AbstractQueryHandler<GetNotification, NotificationDetails> {

    private static final String NOTIFICATION_NOT_FOUND = "Notification to be viewed was not found";
    private static final String ID_REQUIRED = "Notification ID cannot be null";

    private final NotificationReader reader;

    public GetNotificationHandler(NotificationReader reader) {
        this.reader = reader;
    }

    @Override
    public void validate(GetNotification query) {

        if (query.notificationId() == null) {
            throw new IllegalArgumentException(ID_REQUIRED);
        }
    }

    @Override
    public NotificationDetails execute(GetNotification query) {

        return reader.getNotificationByPublicId(query.notificationId(), query.requesterUsername(), query.requesterRole())
                .orElseThrow(() -> new NotFoundException(NOTIFICATION_NOT_FOUND));
    }
}
