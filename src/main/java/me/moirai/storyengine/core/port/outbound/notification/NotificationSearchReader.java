package me.moirai.storyengine.core.port.outbound.notification;

import me.moirai.storyengine.common.dto.PaginatedResult;
import me.moirai.storyengine.core.port.inbound.notification.SearchNotifications;

public interface NotificationSearchReader {

    PaginatedResult<NotificationSearchRow> search(SearchNotifications query);
}
