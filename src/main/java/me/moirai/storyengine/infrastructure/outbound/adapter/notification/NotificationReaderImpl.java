package me.moirai.storyengine.infrastructure.outbound.adapter.notification;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import me.moirai.storyengine.common.dbutil.Filter;
import me.moirai.storyengine.common.dbutil.Filters;
import me.moirai.storyengine.common.dbutil.QueryBuilder;
import me.moirai.storyengine.common.dbutil.SqlArrays;
import me.moirai.storyengine.common.enums.Role;
import me.moirai.storyengine.common.util.Functions;
import me.moirai.storyengine.core.domain.notification.NotificationLevel;
import me.moirai.storyengine.core.domain.notification.NotificationType;
import me.moirai.storyengine.core.port.inbound.notification.NotificationDetails;
import me.moirai.storyengine.core.port.outbound.notification.NotificationReader;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@Repository
public class NotificationReaderImpl implements NotificationReader {

    //@formatter:off
    private static final String SELECT = """
            SELECT n.public_id,
                   n.message,
                   n.type,
                   n.level,
                   COALESCE((
                       SELECT array_agg(mu.username)
                         FROM notification_recipient rcp
                         JOIN moirai_user mu ON mu.id = rcp.user_id
                        WHERE rcp.notification_id = n.id
                   ), ARRAY[]::varchar[]) AS target_usernames,
                   a.public_id AS adventure_id,
                   n.is_interactable,
                   n.metadata,
                   n.creation_date,
                   n.last_update_date
              FROM notification n
              LEFT JOIN adventure a ON n.adventure_id = a.id
              JOIN moirai_user req ON req.username = :requesterUsername
              """;
    //@formatter:on

    private final JdbcClient jdbcClient;
    private final JsonMapper jsonMapper;

    public NotificationReaderImpl(
            JdbcClient jdbcClient,
            JsonMapper jsonMapper) {

        this.jdbcClient = jdbcClient;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public Optional<NotificationDetails> getNotificationByPublicId(
            UUID publicId,
            String requesterUsername,
            Role requesterRole) {

        var queryBuilder = QueryBuilder.select(SELECT)
                .filter(Filters.equals("n.public_id", "publicId", publicId));

        if (!requesterRole.equals(Role.ADMIN)) {
            queryBuilder.filter(new Filter("n.type <> 'GAME'"))
                    .filter(new Filter(
                            "(n.type = 'BROADCAST' OR EXISTS ("
                                    + "SELECT 1 FROM notification_recipient nr2 "
                                    + "WHERE nr2.notification_id = n.id AND nr2.user_id = req.id))"));
        }

        var builtQuery = queryBuilder.build();
        var params = builtQuery.parameters();
        params.put("requesterUsername", requesterUsername);

        return jdbcClient.sql(builtQuery.sql())
                .params(params)
                .query((rs, _) -> {

                    var type = NotificationType.valueOf(rs.getString("type"));
                    var aggregated = Functions.mapOrDefault(rs.getArray("target_usernames"), List.<String>of(),
                            arr -> SqlArrays.toList(arr, String.class));

                    var targetUsernames = requesterRole.equals(Role.ADMIN)
                            ? aggregated
                            : (type == NotificationType.SYSTEM ? List.of(requesterUsername) : List.<String>of());

                    return new NotificationDetails(
                            rs.getObject("public_id", UUID.class),
                            rs.getString("message"),
                            type,
                            Functions.mapOrNull(rs.getString("level"), NotificationLevel::valueOf),
                            targetUsernames,
                            rs.getObject("adventure_id", UUID.class),
                            rs.getBoolean("is_interactable"),
                            Functions.mapOrNull(rs.getString("metadata"),
                                    s -> jsonMapper.readValue(s, new TypeReference<Map<String, Object>>() {
                                    })),
                            rs.getTimestamp("creation_date").toInstant(),
                            rs.getTimestamp("last_update_date").toInstant());
                })
                .optional();
    }
}
