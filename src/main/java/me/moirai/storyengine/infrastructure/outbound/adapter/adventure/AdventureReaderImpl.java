package me.moirai.storyengine.infrastructure.outbound.adapter.adventure;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import me.moirai.storyengine.core.port.inbound.adventure.AdventureDetails;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureReader;

@Repository
public class AdventureReaderImpl implements AdventureReader {

    //@formatter:off
    private static final String SELECT_BY_ID = """
            SELECT  a.public_id,
                    a.name,
                    a.description,
                    a.adventure_start,
                    w.public_id AS world_public_id,
                    p.public_id AS persona_public_id,
                    a.channel_id,
                    a.visibility,
                    a.ai_model,
                    a.moderation,
                    a.game_mode,
                    a.owner_id,
                    a.nudge,
                    a.remember,
                    a.authors_note,
                    a.bump,
                    a.bump_frequency,
                    a.max_token_limit,
                    a.temperature,
                    a.frequency_penalty,
                    a.presence_penalty,
                    a.is_multiplayer,
                    a.logit_bias,
                    a.stop_sequences,
                    a.users_allowed_to_read,
                    a.users_allowed_to_write,
                    a.creation_date,
                    a.last_update_date
               FROM adventure a
               JOIN world   w ON a.world_id   = w.id
               JOIN persona p ON a.persona_id = p.id
              WHERE a.public_id = :publicId
            """;
    //@formatter:on

    private final JdbcClient jdbcClient;

    public AdventureReaderImpl(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Optional<AdventureDetails> getAdventureById(UUID publicId) {
        return jdbcClient.sql(SELECT_BY_ID)
                .param("publicId", publicId)
                .query(toAdventureDetails())
                .optional();
    }

    private RowMapper<AdventureDetails> toAdventureDetails() {
        return (rs, _) -> new AdventureDetails(
                UUID.fromString(rs.getString("public_id")),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("adventure_start"),
                UUID.fromString(rs.getString("world_public_id")),
                UUID.fromString(rs.getString("persona_public_id")),
                rs.getString("channel_id"),
                rs.getString("visibility"),
                rs.getString("ai_model"),
                rs.getString("moderation"),
                rs.getString("game_mode"),
                rs.getString("owner_id"),
                rs.getString("nudge"),
                rs.getString("remember"),
                rs.getString("authors_note"),
                rs.getString("bump"),
                rs.getInt("bump_frequency"),
                rs.getInt("max_token_limit"),
                rs.getDouble("temperature"),
                rs.getDouble("frequency_penalty"),
                rs.getDouble("presence_penalty"),
                rs.getBoolean("is_multiplayer"),
                rs.getObject("creation_date", OffsetDateTime.class),
                rs.getObject("last_update_date", OffsetDateTime.class),
                parseLogitBias(rs.getString("logit_bias")),
                parseStringSet(rs.getString("stop_sequences")),
                parseStringSet(rs.getString("users_allowed_to_read")),
                parseStringSet(rs.getString("users_allowed_to_write")));
    }

    private Map<String, Double> parseLogitBias(String value) {
        if (value == null || value.isBlank()) return Map.of();
        return Arrays.stream(value.split(","))
                .map(s -> s.split("="))
                .collect(Collectors.toMap(s -> s[0], s -> Double.valueOf(s[1])));
    }

    private Set<String> parseStringSet(String value) {
        if (value == null || value.isBlank()) return Set.of();
        return Arrays.stream(value.split(",")).map(String::trim).collect(Collectors.toSet());
    }
}
