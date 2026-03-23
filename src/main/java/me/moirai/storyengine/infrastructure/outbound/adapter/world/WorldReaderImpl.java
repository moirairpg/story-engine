package me.moirai.storyengine.infrastructure.outbound.adapter.world;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

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
                   w.owner_id,
                   w.users_allowed_to_read,
                   w.users_allowed_to_write,
                   w.creation_date,
                   w.last_update_date
              FROM world w
             WHERE w.public_id = :publicId
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
        return (rs, _) -> new WorldDetails(
                UUID.fromString(rs.getString("public_id")),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("adventure_start"),
                rs.getString("visibility"),
                rs.getString("owner_id"),
                parseStringSet(rs.getString("users_allowed_to_read")),
                parseStringSet(rs.getString("users_allowed_to_write")),
                rs.getTimestamp("creation_date").toInstant(),
                rs.getTimestamp("last_update_date").toInstant());
    }

    private Set<String> parseStringSet(String value) {
        if (value == null || value.isBlank()) return Set.of();
        return Arrays.stream(value.split(",")).map(String::trim).collect(Collectors.toSet());
    }
}
