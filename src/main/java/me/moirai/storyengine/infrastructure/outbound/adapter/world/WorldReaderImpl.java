package me.moirai.storyengine.infrastructure.outbound.adapter.world;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import me.moirai.storyengine.common.domain.Permission;
import me.moirai.storyengine.common.enums.PermissionLevel;
import me.moirai.storyengine.core.port.inbound.world.WorldDetails;
import me.moirai.storyengine.core.port.outbound.world.WorldReader;

@Repository
public class WorldReaderImpl implements WorldReader {

    //@formatter:off
    private static final String SELECT_BY_ID = """
            SELECT w.public_id,
                   w.name,
                   w.description,
                   w.adventure_start,
                   w.visibility,
                   w.id AS numeric_id,
                   w.creation_date,
                   w.last_update_date
              FROM world w
             WHERE w.public_id = :publicId
            """;

    private static final String SELECT_PERMISSIONS = """
            SELECT wp.user_id, wp.permission
              FROM world_permissions wp
             WHERE wp.world_id = :worldId
            """;
    //@formatter:on

    private final JdbcClient jdbcClient;

    public WorldReaderImpl(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Optional<WorldDetails> getWorldById(UUID publicId) {
        return jdbcClient.sql(SELECT_BY_ID)
                .param("publicId", publicId)
                .query(toWorldDetails())
                .optional();
    }

    private RowMapper<WorldDetails> toWorldDetails() {
        return (rs, _) -> {
            var numericId = rs.getLong("numeric_id");
            var permissions = new HashSet<>(jdbcClient.sql(SELECT_PERMISSIONS)
                    .param("worldId", numericId)
                    .query((r, __) -> new Permission(r.getLong("user_id"), PermissionLevel.valueOf(r.getString("permission"))))
                    .list());
            return new WorldDetails(
                    UUID.fromString(rs.getString("public_id")),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("adventure_start"),
                    rs.getString("visibility"),
                    permissions,
                    rs.getTimestamp("creation_date").toInstant(),
                    rs.getTimestamp("last_update_date").toInstant());
        };
    }
}
