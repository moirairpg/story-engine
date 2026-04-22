package me.moirai.storyengine.infrastructure.outbound.adapter.notification;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import me.moirai.storyengine.common.util.Functions;
import me.moirai.storyengine.core.domain.notification.NotificationType;
import me.moirai.storyengine.core.port.inbound.notification.NotificationAuthorizationData;
import me.moirai.storyengine.core.port.outbound.notification.NotificationAuthorizationReader;

@Repository
public class NotificationAuthorizationReaderImpl implements NotificationAuthorizationReader {

    //@formatter:off
    private static final String SELECT_AUTH_DATA = """
            SELECT n.target_user_id,
                   n.type
              FROM notification n
             WHERE n.public_id = :publicId
            """;
    //@formatter:on

    private final JdbcClient jdbcClient;

    public NotificationAuthorizationReaderImpl(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Optional<NotificationAuthorizationData> getAuthorizationData(UUID publicId) {
        return jdbcClient.sql(SELECT_AUTH_DATA)
                .param("publicId", publicId)
                .query((rs, _) -> new NotificationAuthorizationData(
                        Functions.mapOrNull(rs.getBigDecimal("target_user_id"), BigDecimal::longValue),
                        NotificationType.valueOf(rs.getString("type"))))
                .optional();
    }
}
