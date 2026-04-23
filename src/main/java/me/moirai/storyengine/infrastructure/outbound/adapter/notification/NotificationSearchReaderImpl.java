package me.moirai.storyengine.infrastructure.outbound.adapter.notification;

import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import me.moirai.storyengine.common.dbutil.Filter;
import me.moirai.storyengine.common.dbutil.Filters;
import me.moirai.storyengine.common.dbutil.PaginatedQuery;
import me.moirai.storyengine.common.dto.PaginatedResult;
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
                   n.creation_date
              FROM notification n
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

    private Optional<Filter> resolveReceiverFilter(UUID receiverId) {

        if (receiverId == null) {
            return Optional.empty();
        }

        return Optional.of(new Filter(
                "EXISTS (SELECT 1 FROM notification_recipient nr_f "
                        + "JOIN moirai_user u ON u.id = nr_f.user_id "
                        + "WHERE nr_f.notification_id = n.id AND u.public_id = :receiverId)",
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
