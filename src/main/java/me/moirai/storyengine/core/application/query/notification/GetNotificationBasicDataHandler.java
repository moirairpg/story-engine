package me.moirai.storyengine.core.application.query.notification;

import me.moirai.storyengine.common.annotation.QueryHandler;
import me.moirai.storyengine.common.cqs.query.AbstractQueryHandler;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.port.inbound.notification.GetNotificationBasicData;
import me.moirai.storyengine.core.port.inbound.notification.NotificationBasicData;
import me.moirai.storyengine.core.port.outbound.notification.NotificationBasicDataReader;

@QueryHandler
public class GetNotificationBasicDataHandler extends AbstractQueryHandler<GetNotificationBasicData, NotificationBasicData> {

    private static final String NOTIFICATION_NOT_FOUND = "Notification not found";
    private static final String ID_REQUIRED = "Notification ID cannot be null";

    private final NotificationBasicDataReader reader;

    public GetNotificationBasicDataHandler(NotificationBasicDataReader reader) {
        this.reader = reader;
    }

    @Override
    public void validate(GetNotificationBasicData query) {

        if (query.notificationId() == null) {
            throw new IllegalArgumentException(ID_REQUIRED);
        }
    }

    @Override
    public NotificationBasicData execute(GetNotificationBasicData query) {

        return reader.getByPublicId(query.notificationId())
                .orElseThrow(() -> new NotFoundException(NOTIFICATION_NOT_FOUND));
    }
}
