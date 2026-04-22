package me.moirai.storyengine.core.application.query.notification;

import java.util.List;

import me.moirai.storyengine.common.annotation.QueryHandler;
import me.moirai.storyengine.common.cqs.query.AbstractQueryHandler;
import me.moirai.storyengine.core.port.inbound.notification.GetActiveSystemNotifications;
import me.moirai.storyengine.core.port.inbound.notification.NotificationDetails;
import me.moirai.storyengine.core.port.outbound.notification.ActiveSystemNotificationReader;

@QueryHandler
public class GetActiveSystemNotificationsHandler
        extends AbstractQueryHandler<GetActiveSystemNotifications, List<NotificationDetails>> {

    private final ActiveSystemNotificationReader reader;

    public GetActiveSystemNotificationsHandler(ActiveSystemNotificationReader reader) {
        this.reader = reader;
    }

    @Override
    public List<NotificationDetails> execute(GetActiveSystemNotifications query) {
        return reader.getActiveUnreadSystemNotifications(query.userId());
    }
}
