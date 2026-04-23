package me.moirai.storyengine.core.port.outbound.notification;

import me.moirai.storyengine.common.dto.PaginatedResult;
import me.moirai.storyengine.core.port.inbound.notification.NotificationSummary;
import me.moirai.storyengine.core.port.inbound.notification.SearchNotifications;

public interface NotificationSearchReader {

    PaginatedResult<NotificationSummary> search(SearchNotifications query);
}
