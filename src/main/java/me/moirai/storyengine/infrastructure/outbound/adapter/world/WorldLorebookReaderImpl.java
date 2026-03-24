package me.moirai.storyengine.infrastructure.outbound.adapter.world;

import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import me.moirai.storyengine.core.port.inbound.world.WorldLorebookEntryDetails;
import me.moirai.storyengine.core.port.outbound.world.WorldLorebookReader;

@Repository
public class WorldLorebookReaderImpl implements WorldLorebookReader {

    //@formatter:off
    private static final String SELECT_BY_ID = """
            SELECT wl.public_id,
                   wl.name,
                   wl.description,
                   wl.regex,
                    w.public_id AS world_public_id,
                   wl.creation_date,
                   wl.last_update_date
              FROM world_lorebook wl
              JOIN world w ON wl.world_id = w.id
             WHERE wl.public_id = :entryPublicId
               AND  w.public_id = :worldPublicId
            """;
    //@formatter:on

    private final JdbcClient jdbcClient;

    public WorldLorebookReaderImpl(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Optional<WorldLorebookEntryDetails> getWorldLorebookEntryById(UUID entryPublicId, UUID worldPublicId) {
        return jdbcClient.sql(SELECT_BY_ID)
                .param("entryPublicId", entryPublicId)
                .param("worldPublicId", worldPublicId)
                .query(toWorldLorebookEntryDetails())
                .optional();
    }

    private RowMapper<WorldLorebookEntryDetails> toWorldLorebookEntryDetails() {
        return (rs, _) -> new WorldLorebookEntryDetails(
                UUID.fromString(rs.getString("public_id")),
                UUID.fromString(rs.getString("world_public_id")),
                rs.getString("name"),
                rs.getString("regex"),
                rs.getString("description"),
                rs.getTimestamp("creation_date").toInstant(),
                rs.getTimestamp("last_update_date").toInstant());
    }
}
