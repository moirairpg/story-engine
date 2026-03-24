package me.moirai.storyengine.infrastructure.outbound.adapter.userdetails;

import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import me.moirai.storyengine.common.enums.Role;
import me.moirai.storyengine.core.port.inbound.userdetails.UserData;
import me.moirai.storyengine.core.port.outbound.userdetails.UserReader;

@Repository
public class UserReaderImpl implements UserReader {

    //@formatter:off
    private static final String SELECT_BY_DISCORD_ID = """
            SELECT u.public_id,
                   u.id,
                   u.discord_id,
                   u.role,
                   u.creation_date
              FROM moirai_user u
             WHERE u.discord_id = :discordId
            """;
    //@formatter:on

    private final JdbcClient jdbcClient;

    public UserReaderImpl(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Optional<UserData> getUserByDiscordId(String discordId) {
        return jdbcClient.sql(SELECT_BY_DISCORD_ID)
                .param("discordId", discordId)
                .query(toUserData())
                .optional();
    }

    private RowMapper<UserData> toUserData() {
        return (rs, _) -> new UserData(
                UUID.fromString(rs.getString("public_id")),
                rs.getLong("id"),
                rs.getString("discord_id"),
                Role.valueOf(rs.getString("role")),
                rs.getTimestamp("creation_date").toInstant());
    }
}
