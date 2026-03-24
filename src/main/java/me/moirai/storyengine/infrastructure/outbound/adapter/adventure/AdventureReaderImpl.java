package me.moirai.storyengine.infrastructure.outbound.adapter.adventure;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import me.moirai.storyengine.common.enums.ArtificialIntelligenceModel;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureDetails;
import me.moirai.storyengine.core.port.inbound.adventure.ContextAttributesDto;
import me.moirai.storyengine.core.port.inbound.adventure.ModelConfigurationDto;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureReader;

@Repository
public class AdventureReaderImpl implements AdventureReader {

    //@formatter:off
    private static final String SELECT_BY_ID = """
            SELECT  a.id,
                    a.public_id,
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
                    a.users_allowed_to_read,
                    a.users_allowed_to_write,
                    a.creation_date,
                    a.last_update_date
               FROM adventure a
               JOIN world   w ON a.world_id   = w.id
               JOIN persona p ON a.persona_id = p.id
              WHERE a.public_id = :publicId
            """;

    private static final String SELECT_STOP_SEQUENCES = """
            SELECT value FROM adventure_stop_sequences WHERE adventure_id = :adventureId
            """;

    private static final String SELECT_LOGIT_BIAS = """
            SELECT token_id, bias FROM adventure_logit_bias WHERE adventure_id = :adventureId
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
        return (rs, _) -> {
            var adventureId = rs.getLong("id");

            var stopSequences = new HashSet<>(jdbcClient.sql(SELECT_STOP_SEQUENCES)
                    .param("adventureId", adventureId)
                    .query((r, __) -> r.getString("value"))
                    .list());

            var logitBias = jdbcClient.sql(SELECT_LOGIT_BIAS)
                    .param("adventureId", adventureId)
                    .query((r, __) -> Map.entry(r.getString("token_id"), r.getDouble("bias")))
                    .list()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            var modelConfiguration = new ModelConfigurationDto(
                    ArtificialIntelligenceModel.fromString(rs.getString("ai_model")),
                    rs.getInt("max_token_limit"),
                    rs.getDouble("temperature"),
                    rs.getDouble("frequency_penalty"),
                    rs.getDouble("presence_penalty"),
                    stopSequences,
                    logitBias);

            var contextAttributes = new ContextAttributesDto(
                    rs.getString("nudge"),
                    rs.getString("authors_note"),
                    rs.getString("remember"),
                    rs.getString("bump"),
                    rs.getObject("bump_frequency", Integer.class));

            return new AdventureDetails(
                    UUID.fromString(rs.getString("public_id")),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("adventure_start"),
                    UUID.fromString(rs.getString("world_public_id")),
                    UUID.fromString(rs.getString("persona_public_id")),
                    rs.getString("channel_id"),
                    rs.getString("visibility"),
                    rs.getString("moderation"),
                    rs.getString("game_mode"),
                    rs.getString("owner_id"),
                    rs.getBoolean("is_multiplayer"),
                    rs.getTimestamp("creation_date").toInstant(),
                    rs.getTimestamp("last_update_date").toInstant(),
                    modelConfiguration,
                    contextAttributes,
                    parseStringSet(rs.getString("users_allowed_to_read")),
                    parseStringSet(rs.getString("users_allowed_to_write")));
        };
    }

    private Set<String> parseStringSet(String value) {
        if (value == null || value.isBlank()) return Set.of();
        return Arrays.stream(value.split(",")).map(String::trim).collect(Collectors.toSet());
    }
}
