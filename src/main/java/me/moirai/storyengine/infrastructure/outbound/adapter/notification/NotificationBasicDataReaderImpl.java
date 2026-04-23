package me.moirai.storyengine.infrastructure.outbound.adapter.notification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import me.moirai.storyengine.common.dbutil.SqlArrays;
import me.moirai.storyengine.common.util.Functions;
import me.moirai.storyengine.core.domain.notification.NotificationType;
import me.moirai.storyengine.core.port.inbound.notification.NotificationBasicData;
import me.moirai.storyengine.core.port.outbound.notification.NotificationBasicDataReader;

@Repository
public class NotificationBasicDataReaderImpl implements NotificationBasicDataReader {

    //@formatter:off
    private static final String SELECT = """
            SELECT n.type,
                   COALESCE((
                       SELECT array_agg(mu.username)
                         FROM notification_recipient rcp
                         JOIN moirai_user mu ON mu.id = rcp.user_id
                        WHERE rcp.notification_id = n.id
                   ), ARRAY[]::varchar[]) AS target_usernames
              FROM notification n
             WHERE n.public_id = :publicId
            """;
    //@formatter:on

    private final JdbcClient jdbcClient;

    public NotificationBasicDataReaderImpl(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Optional<NotificationBasicData> getByPublicId(UUID publicId) {

        return jdbcClient.sql(SELECT)
                .param("publicId", publicId)
                .query((rs, _) -> new NotificationBasicData(
                        Functions.mapOrDefault(rs.getArray("target_usernames"), List.of(),
                                arr -> SqlArrays.toList(arr, String.class)),
                        NotificationType.valueOf(rs.getString("type"))))
                .optional();
    }
}
