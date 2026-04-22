package me.moirai.storyengine.core.application.query.notification;

import me.moirai.storyengine.common.annotation.QueryHandler;
import me.moirai.storyengine.common.cqs.query.AbstractQueryHandler;
import me.moirai.storyengine.common.dto.PaginatedResult;
import me.moirai.storyengine.core.port.inbound.notification.NotificationSummary;
import me.moirai.storyengine.core.port.inbound.notification.SearchNotifications;
import me.moirai.storyengine.core.port.outbound.notification.NotificationSearchReader;

@QueryHandler
public class SearchNotificationsHandler
        extends AbstractQueryHandler<SearchNotifications, PaginatedResult<NotificationSummary>> {

    private final NotificationSearchReader reader;

    public SearchNotificationsHandler(NotificationSearchReader reader) {
        this.reader = reader;
    }

    @Override
    public PaginatedResult<NotificationSummary> execute(SearchNotifications query) {

        var rows = reader.search(query);

        var summaries = rows.data().stream()
                .map(row -> new NotificationSummary(
                        row.publicId(),
                        row.message(),
                        row.type(),
                        row.level(),
                        row.status(),
                        row.creationDate()))
                .toList();

        return new PaginatedResult<>(summaries, rows.items(), rows.totalItems(), rows.page(), rows.totalPages());
    }
}
