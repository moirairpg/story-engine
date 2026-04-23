package me.moirai.storyengine.infrastructure.outbound.adapter.notification;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import me.moirai.storyengine.common.dbutil.Filter;
import me.moirai.storyengine.common.dbutil.Filters;
import me.moirai.storyengine.common.dbutil.QueryBuilder;
import me.moirai.storyengine.common.enums.NotificationStatus;
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
                   u.username,
                   a.public_id AS adventure_id,
                   n.is_interactable,
                   n.metadata,
                   n.creation_date,
                   n.last_update_date,
                   CASE WHEN nr.id IS NOT NULL THEN 'READ' ELSE 'UNREAD' END AS status
              FROM notification n
              LEFT JOIN adventure a ON n.adventure_id = a.id
              LEFT JOIN moirai_user u ON n.target_user_id = u.id
              JOIN moirai_user req ON req.username = :requesterUsername
              LEFT JOIN notification_read nr ON nr.notification_id = n.id AND nr.user_id = req.id
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
                    .filter(new Filter("(n.target_user_id IS NULL OR n.target_user_id = req.id)"));
        }

        var builtQuery = queryBuilder.build();
        var params = builtQuery.parameters();
        params.put("requesterUsername", requesterUsername);

        return jdbcClient.sql(builtQuery.sql())
                .params(params)
                .query((rs, _) -> new NotificationDetails(
                        rs.getObject("public_id", UUID.class),
                        rs.getString("message"),
                        NotificationType.valueOf(rs.getString("type")),
                        Functions.mapOrNull(rs.getString("level"), NotificationLevel::valueOf),
                        NotificationStatus.valueOf(rs.getString("status")),
                        rs.getString("username"),
                        rs.getObject("adventure_id", UUID.class),
                        rs.getBoolean("is_interactable"),
                        Functions.mapOrNull(rs.getString("metadata"),
                                s -> jsonMapper.readValue(s, new TypeReference<Map<String, Object>>() {
                                })),
                        rs.getTimestamp("creation_date").toInstant(),
                        rs.getTimestamp("last_update_date").toInstant()))
                .optional();
    }
}
