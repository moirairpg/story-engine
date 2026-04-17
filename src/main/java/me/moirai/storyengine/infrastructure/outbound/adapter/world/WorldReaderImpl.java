package me.moirai.storyengine.infrastructure.outbound.adapter.world;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import me.moirai.storyengine.common.dto.PermissionDto;
import me.moirai.storyengine.common.enums.PermissionLevel;
import me.moirai.storyengine.common.util.Functions;
import me.moirai.storyengine.core.port.inbound.world.WorldDetails;
import me.moirai.storyengine.core.port.inbound.world.WorldLorebookEntryDetails;
import me.moirai.storyengine.core.port.outbound.storage.StoragePort;
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
                   w.narrator_name,
                   w.narrator_personality,
                   w.image_key,
                   w.ui_image_position_x,
                   w.ui_image_position_y,
                   w.id AS numeric_id,
                   w.creation_date,
                   w.last_update_date
              FROM world w
             WHERE w.public_id = :publicId
            """;

    private static final String SELECT_LOREBOOK = """
          SELECT wl.public_id,
                 wl.name,
                 wl.description,
                 wl.creation_date,
                 wl.last_update_date
            FROM world_lorebook wl
           WHERE wl.world_id = :worldId
          """;

    private static final String SELECT_PERMISSIONS = """
            SELECT w.public_id,
                   wp.permission
              FROM world_permissions wp
                   INNER JOIN world w ON wp.world_id = w.id
             WHERE wp.world_id = :worldId
            """;
    //@formatter:on

    private final JdbcClient jdbcClient;
    private final StoragePort storagePort;

    public WorldReaderImpl(JdbcClient jdbcClient, StoragePort storagePort) {
        this.jdbcClient = jdbcClient;
        this.storagePort = storagePort;
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
                    .query((r, __) -> new PermissionDto(UUID.fromString(r.getString("public_id")),
                            PermissionLevel.valueOf(r.getString("permission"))))
                    .list());

            var lorebook = new HashSet<>(jdbcClient.sql(SELECT_LOREBOOK)
                    .param("worldId", numericId)
                    .query((r, __) -> new WorldLorebookEntryDetails(
                            UUID.fromString(r.getString("public_id")),
                            UUID.fromString(rs.getString("public_id")),
                            r.getString("name"),
                            r.getString("description"),
                            r.getTimestamp("creation_date").toInstant(),
                            r.getTimestamp("last_update_date").toInstant()))
                    .list());

            return new WorldDetails(
                    UUID.fromString(rs.getString("public_id")),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("adventure_start"),
                    rs.getString("narrator_name"),
                    rs.getString("narrator_personality"),
                    rs.getString("visibility"),
                    storagePort.resolveUrl(rs.getString("image_key")),
                    permissions,
                    lorebook,
                    rs.getTimestamp("creation_date").toInstant(),
                    rs.getTimestamp("last_update_date").toInstant(),
                    Functions.mapOrNull(rs.getBigDecimal("ui_image_position_x"), BigDecimal::doubleValue),
                    Functions.mapOrNull(rs.getBigDecimal("ui_image_position_y"), BigDecimal::doubleValue));
        };
    }
}
