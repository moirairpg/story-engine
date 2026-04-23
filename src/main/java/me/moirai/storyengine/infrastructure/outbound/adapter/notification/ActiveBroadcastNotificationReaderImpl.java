package me.moirai.storyengine.infrastructure.outbound.adapter.notification;

import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import me.moirai.storyengine.common.enums.NotificationStatus;
import me.moirai.storyengine.common.util.Functions;
import me.moirai.storyengine.core.domain.notification.NotificationLevel;
import me.moirai.storyengine.core.domain.notification.NotificationType;
import me.moirai.storyengine.core.port.inbound.notification.NotificationDetails;
import me.moirai.storyengine.core.port.outbound.notification.ActiveBroadcastNotificationReader;

@Repository
public class ActiveBroadcastNotificationReaderImpl implements ActiveBroadcastNotificationReader {

    //@formatter:off
    private static final String SELECT_BROADCASTS = """
            SELECT n.public_id,
                   n.message,
                   n.type,
                   n.level,
                   a.public_id AS adventure_id,
                   n.is_interactable,
                   n.creation_date,
                   n.last_update_date
              FROM notification n
              LEFT JOIN adventure a ON n.adventure_id = a.id
              JOIN moirai_user req ON req.username = :username
             WHERE n.type = 'BROADCAST'
               AND NOT EXISTS (
                   SELECT 1 FROM notification_read nr
                    WHERE nr.notification_id = n.id
                      AND nr.user_id = req.id
               )
            """;
    //@formatter:on

    private final JdbcClient jdbcClient;

    public ActiveBroadcastNotificationReaderImpl(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public List<NotificationDetails> getActiveBroadcasts(String username) {
        return jdbcClient.sql(SELECT_BROADCASTS)
                .param("username", username)
                .query((rs, _) -> new NotificationDetails(
                        rs.getObject("public_id", UUID.class),
                        rs.getString("message"),
                        NotificationType.valueOf(rs.getString("type")),
                        Functions.mapOrNull(rs.getString("level"), NotificationLevel::valueOf),
                        NotificationStatus.UNREAD,
                        null,
                        rs.getObject("adventure_id", UUID.class),
                        rs.getBoolean("is_interactable"),
                        null,
                        rs.getTimestamp("creation_date").toInstant(),
                        rs.getTimestamp("last_update_date").toInstant()))
                .list();
    }
}
