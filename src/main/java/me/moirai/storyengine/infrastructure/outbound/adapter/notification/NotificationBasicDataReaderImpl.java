package me.moirai.storyengine.infrastructure.outbound.adapter.notification;

import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import me.moirai.storyengine.core.domain.notification.NotificationType;
import me.moirai.storyengine.core.port.inbound.notification.NotificationBasicData;
import me.moirai.storyengine.core.port.outbound.notification.NotificationBasicDataReader;

@Repository
public class NotificationBasicDataReaderImpl implements NotificationBasicDataReader {

    //@formatter:off
    private static final String SELECT = """
            SELECT u.username,
                   n.type
              FROM notification n
              LEFT JOIN moirai_user u ON n.target_user_id = u.id
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
                        rs.getString("username"),
                        NotificationType.valueOf(rs.getString("type"))))
                .optional();
    }
}
