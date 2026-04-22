package me.moirai.storyengine.infrastructure.outbound.adapter.notification;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import me.moirai.storyengine.common.enums.NotificationStatus;
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
    private static final String SELECT_BY_PUBLIC_ID_ADMIN = """
            SELECT n.public_id,
                   n.message,
                   n.type,
                   n.level,
                   n.target_user_id,
                   n.adventure_id,
                   n.is_interactable,
                   n.metadata,
                   n.creation_date,
                   n.last_update_date,
                   CASE WHEN nr.id IS NOT NULL THEN 'READ' ELSE 'UNREAD' END AS status
              FROM notification n
              LEFT JOIN notification_read nr ON nr.notification_id = n.id
                        AND nr.user_id = :requesterId
             WHERE n.public_id = :publicId
            """;

    private static final String SELECT_BY_PUBLIC_ID_USER = """
            SELECT n.public_id,
                   n.message,
                   n.type,
                   n.level,
                   n.target_user_id,
                   n.adventure_id,
                   n.is_interactable,
                   n.metadata,
                   n.creation_date,
                   n.last_update_date,
                   CASE WHEN nr.id IS NOT NULL THEN 'READ' ELSE 'UNREAD' END AS status
              FROM notification n
              LEFT JOIN notification_read nr ON nr.notification_id = n.id
                        AND nr.user_id = :requesterId
             WHERE n.public_id = :publicId
               AND n.type <> 'GAME'
               AND (n.target_user_id IS NULL OR n.target_user_id = :requesterId)
            """;
    //@formatter:on

    private final JdbcClient jdbcClient;
    private final JsonMapper jsonMapper;

    public NotificationReaderImpl(JdbcClient jdbcClient, JsonMapper jsonMapper) {
        this.jdbcClient = jdbcClient;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public Optional<NotificationDetails> getNotificationByPublicId(UUID publicId, Long requesterId, boolean isAdmin) {

        var sql = isAdmin ? SELECT_BY_PUBLIC_ID_ADMIN : SELECT_BY_PUBLIC_ID_USER;

        return jdbcClient.sql(sql)
                .param("publicId", publicId)
                .param("requesterId", requesterId)
                .query((rs, _) -> new NotificationDetails(
                        UUID.fromString(rs.getString("public_id")),
                        rs.getString("message"),
                        NotificationType.valueOf(rs.getString("type")),
                        Functions.mapOrNull(rs.getString("level"), NotificationLevel::valueOf),
                        NotificationStatus.valueOf(rs.getString("status")),
                        Functions.mapOrNull(rs.getBigDecimal("target_user_id"), BigDecimal::longValue),
                        Functions.mapOrNull(rs.getBigDecimal("adventure_id"), BigDecimal::longValue),
                        rs.getBoolean("is_interactable"),
                        Functions.mapOrNull(rs.getString("metadata"),
                                s -> jsonMapper.readValue(s, new TypeReference<Map<String, Object>>() {
                                })),
                        rs.getTimestamp("creation_date").toInstant(),
                        rs.getTimestamp("last_update_date").toInstant()))
                .optional();
    }
}
