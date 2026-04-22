package me.moirai.storyengine.core.application.query.notification;

import java.util.List;

import me.moirai.storyengine.common.annotation.QueryHandler;
import me.moirai.storyengine.common.cqs.query.AbstractQueryHandler;
import me.moirai.storyengine.core.port.inbound.notification.GetActiveBroadcastNotifications;
import me.moirai.storyengine.core.port.inbound.notification.NotificationDetails;
import me.moirai.storyengine.core.port.outbound.notification.ActiveBroadcastNotificationReader;

@QueryHandler
public class GetActiveBroadcastNotificationsHandler
        extends AbstractQueryHandler<GetActiveBroadcastNotifications, List<NotificationDetails>> {

    private final ActiveBroadcastNotificationReader reader;

    public GetActiveBroadcastNotificationsHandler(ActiveBroadcastNotificationReader reader) {
        this.reader = reader;
    }

    @Override
    public List<NotificationDetails> execute(GetActiveBroadcastNotifications query) {
        return reader.getActiveBroadcasts(query.requesterId());
    }
}
