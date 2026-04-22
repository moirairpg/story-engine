package me.moirai.storyengine.core.port.inbound.notification;

import java.util.UUID;

import me.moirai.storyengine.common.cqs.query.Query;
import me.moirai.storyengine.common.dto.PaginatedResult;
import me.moirai.storyengine.common.enums.NotificationStatus;
import me.moirai.storyengine.common.enums.SortDirection;
import me.moirai.storyengine.core.domain.notification.NotificationLevel;
import me.moirai.storyengine.core.domain.notification.NotificationType;

public record SearchNotifications(
        NotificationType type,
        NotificationLevel level,
        NotificationStatus status,
        UUID receiverId,
        Long requesterId,
        boolean isAdmin,
        NotificationSortField sortingField,
        SortDirection direction,
        Integer page,
        Integer size)
        implements Query<PaginatedResult<NotificationSummary>> {
}
