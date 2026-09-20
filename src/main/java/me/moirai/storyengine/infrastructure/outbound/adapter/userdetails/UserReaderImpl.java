package me.moirai.storyengine.infrastructure.outbound.adapter.userdetails;

import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import me.moirai.storyengine.common.dbutil.Filter;
import me.moirai.storyengine.common.dbutil.QueryBuilder;
import me.moirai.storyengine.common.enums.Role;
import me.moirai.storyengine.core.port.inbound.userdetails.UserData;
import me.moirai.storyengine.core.port.outbound.userdetails.UserReader;

@Repository
public class UserReaderImpl implements UserReader {

    //@formatter:off
    private static final String SELECT_USER = """
            SELECT u.public_id,
                   u.id,
                   u.discord_id,
                   u.username,
                   u.role,
                   u.is_active,
                   u.bio,
                   u.creation_date
              FROM moirai_user u
            """;
    //@formatter:on

    private final JdbcClient jdbcClient;

    public UserReaderImpl(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Optional<UserData> getUserByDiscordId(String discordId) {

        var query = QueryBuilder.select(SELECT_USER)
                .filter(new Filter("u.discord_id = :discordId", "discordId", discordId))
                .build();

        return jdbcClient.sql(query.sql())
                .params(query.parameters())
                .query(toUserData())
                .optional();
    }

    @Override
    public Optional<UserData> getUserById(UUID id) {

        var query = QueryBuilder.select(SELECT_USER)
                .filter(new Filter("u.public_id = :publicId", "publicId", id))
                .build();

        return jdbcClient.sql(query.sql())
                .params(query.parameters())
                .query(toUserData())
                .optional();
    }

    private RowMapper<UserData> toUserData() {
        return (rs, _) -> new UserData(
                UUID.fromString(rs.getString("public_id")),
                rs.getLong("id"),
                rs.getString("discord_id"),
                rs.getString("username"),
                Role.valueOf(rs.getString("role")),
                rs.getBoolean("is_active"),
                rs.getString("bio"),
                rs.getTimestamp("creation_date").toInstant());
    }
}
