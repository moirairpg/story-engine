package me.moirai.storyengine.infrastructure.outbound.adapter.notification;

import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import me.moirai.storyengine.common.dbutil.Filter;
import me.moirai.storyengine.common.dbutil.Filters;
import me.moirai.storyengine.common.dbutil.PaginatedQuery;
import me.moirai.storyengine.common.dto.PaginatedResult;
import me.moirai.storyengine.common.enums.NotificationStatus;
import me.moirai.storyengine.common.util.Functions;
import me.moirai.storyengine.core.domain.notification.NotificationLevel;
import me.moirai.storyengine.core.domain.notification.NotificationType;
import me.moirai.storyengine.core.port.inbound.notification.NotificationSortField;
import me.moirai.storyengine.core.port.inbound.notification.NotificationSummary;
import me.moirai.storyengine.core.port.inbound.notification.SearchNotifications;
import me.moirai.storyengine.core.port.outbound.notification.NotificationSearchReader;

@Repository
public class NotificationSearchReaderImpl implements NotificationSearchReader {

    //@formatter:off
    private static final String SELECT_SQL = """
            SELECT n.public_id,
                   n.message,
                   n.type,
                   n.level,
                   n.creation_date,
                   CASE WHEN nr.id IS NOT NULL THEN 'READ' ELSE 'UNREAD' END AS status
              FROM notification n
              LEFT JOIN notification_read nr ON nr.notification_id = n.id
                        AND nr.user_id = n.target_user_id
            """;
    //@formatter:on

    private final JdbcClient jdbcClient;

    public NotificationSearchReaderImpl(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public PaginatedResult<NotificationSummary> search(SearchNotifications query) {

        var pq = PaginatedQuery.builder()
                .select(SELECT_SQL)
                .filter(resolveTypeFilter(query.type()))
                .filter(Filters.equals("n.level", "level",
                        Functions.mapOrNull(query.level(), NotificationLevel::name)))
                .filter(resolveStatusFilter(query.status()))
                .filter(resolveReceiverFilter(query.receiverId()))
                .sortBy(resolveSortField(query.sortingField()), query.direction())
                .page(query.page(), query.size())
                .build();

        var data = jdbcClient.sql(pq.sql())
                .params(pq.parameters())
                .query((rs, _) -> new NotificationSummary(
                        UUID.fromString(rs.getString("public_id")),
                        rs.getString("message"),
                        NotificationType.valueOf(rs.getString("type")),
                        Functions.mapOrNull(rs.getString("level"), NotificationLevel::valueOf),
                        NotificationStatus.valueOf(rs.getString("status")),
                        rs.getTimestamp("creation_date").toInstant()))
                .list();

        var totalItems = jdbcClient.sql(pq.countSql())
                .params(pq.countParameters())
                .query(Long.class)
                .single();

        return PaginatedResult.of(data, totalItems, pq.page(), pq.size());
    }

    private Optional<Filter> resolveTypeFilter(NotificationType type) {

        if (type == null) {
            return Optional.empty();
        }

        return Filters.equals("n.type", "type", type.name());
    }

    private Optional<Filter> resolveStatusFilter(NotificationStatus status) {

        if (status == null) {
            return Optional.empty();
        }

        if (status == NotificationStatus.READ) {
            return Optional.of(new Filter(
                    "EXISTS (SELECT 1 FROM notification_read nr2 WHERE nr2.notification_id = n.id AND nr2.user_id = n.target_user_id)"));
        }

        return Optional.of(new Filter(
                "NOT EXISTS (SELECT 1 FROM notification_read nr2 WHERE nr2.notification_id = n.id AND nr2.user_id = n.target_user_id)"));
    }

    private Optional<Filter> resolveReceiverFilter(UUID receiverId) {

        if (receiverId == null) {
            return Optional.empty();
        }

        return Optional.of(new Filter(
                "n.target_user_id = (SELECT u.id FROM moirai_user u WHERE u.public_id = :receiverId)",
                "receiverId", receiverId));
    }

    private String resolveSortField(NotificationSortField field) {
        return switch (field) {
            case TYPE -> "n.type";
            case LEVEL -> "n.level";
            case null, default -> "n.creation_date";
        };
    }
}
