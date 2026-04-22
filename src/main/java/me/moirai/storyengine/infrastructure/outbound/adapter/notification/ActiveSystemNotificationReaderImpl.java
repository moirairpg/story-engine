package me.moirai.storyengine.infrastructure.outbound.adapter.notification;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import me.moirai.storyengine.common.enums.NotificationStatus;
import me.moirai.storyengine.common.util.Functions;
import me.moirai.storyengine.core.domain.notification.NotificationLevel;
import me.moirai.storyengine.core.domain.notification.NotificationType;
import me.moirai.storyengine.core.port.inbound.notification.NotificationDetails;
import me.moirai.storyengine.core.port.outbound.notification.ActiveSystemNotificationReader;

@Repository
public class ActiveSystemNotificationReaderImpl implements ActiveSystemNotificationReader {

    //@formatter:off
    private static final String SELECT_UNREAD_SYSTEM = """
            SELECT n.public_id,
                   n.message,
                   n.type,
                   n.level,
                   n.target_user_id,
                   n.adventure_id,
                   n.is_interactable,
                   n.creation_date,
                   n.last_update_date
              FROM notification n
             WHERE n.type = 'SYSTEM'
               AND n.target_user_id = :userId
               AND NOT EXISTS (
                   SELECT 1 FROM notification_read nr
                    WHERE nr.notification_id = n.id
                      AND nr.user_id = :userId
               )
            """;
    //@formatter:on

    private final JdbcClient jdbcClient;

    public ActiveSystemNotificationReaderImpl(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public List<NotificationDetails> getActiveUnreadSystemNotifications(Long userId) {
        return jdbcClient.sql(SELECT_UNREAD_SYSTEM)
                .param("userId", userId)
                .query((rs, _) -> new NotificationDetails(
                        UUID.fromString(rs.getString("public_id")),
                        rs.getString("message"),
                        NotificationType.valueOf(rs.getString("type")),
                        Functions.mapOrNull(rs.getString("level"), NotificationLevel::valueOf),
                        NotificationStatus.UNREAD,
                        Functions.mapOrNull(rs.getBigDecimal("target_user_id"), BigDecimal::longValue),
                        Functions.mapOrNull(rs.getBigDecimal("adventure_id"), BigDecimal::longValue),
                        rs.getBoolean("is_interactable"),
                        null,
                        rs.getTimestamp("creation_date").toInstant(),
                        rs.getTimestamp("last_update_date").toInstant()))
                .list();
    }
}
