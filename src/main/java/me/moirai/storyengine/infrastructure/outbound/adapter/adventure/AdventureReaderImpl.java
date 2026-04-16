package me.moirai.storyengine.infrastructure.outbound.adapter.adventure;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import me.moirai.storyengine.common.dto.PermissionDto;
import me.moirai.storyengine.common.enums.ArtificialIntelligenceModel;
import me.moirai.storyengine.common.enums.Moderation;
import me.moirai.storyengine.common.enums.PermissionLevel;
import me.moirai.storyengine.common.enums.Visibility;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureDetails;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureLorebookEntryDetails;
import me.moirai.storyengine.core.port.inbound.adventure.ContextAttributesDto;
import me.moirai.storyengine.core.port.inbound.adventure.ModelConfigurationDto;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureReader;
import me.moirai.storyengine.core.port.outbound.storage.StoragePort;

@Repository
public class AdventureReaderImpl implements AdventureReader {

    //@formatter:off
    private static final String SELECT_BY_ID = """
            SELECT  a.id,
                    a.public_id,
                    a.name,
                    a.description,
                    a.adventure_start,
                    a.world_id,
                    a.narrator_name,
                    a.narrator_personality,
                    a.visibility,
                    a.ai_model,
                    a.moderation,
                    a.nudge,
                    a.scene,
                    a.authors_note,
                    a.bump,
                    a.bump_frequency,
                    a.max_token_limit,
                    a.temperature,
                    a.is_multiplayer,
                    a.image_key,
                    a.creation_date,
                    a.last_update_date
               FROM adventure a
              WHERE a.public_id = :publicId
            """;

    private static final String SELECT_LOREBOOK = """
          SELECT al.public_id,
                 al.name,
                 al.description,
                 al.player_id,
                 al.is_player_character,
                 al.creation_date,
                 al.last_update_date
            FROM adventure_lorebook al
           WHERE al.adventure_id = :adventureId
          """;

    private static final String SELECT_PERMISSIONS = """
            SELECT a.public_id,
                   ap.permission
              FROM adventure_permissions ap
                   INNER JOIN adventure a ON ap.adventure_id = a.id
             WHERE ap.adventure_id = :adventureId
            """;
    //@formatter:on

    private final JdbcClient jdbcClient;
    private final StoragePort storagePort;

    public AdventureReaderImpl(JdbcClient jdbcClient, StoragePort storagePort) {
        this.jdbcClient = jdbcClient;
        this.storagePort = storagePort;
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

            var permissions = new HashSet<>(jdbcClient.sql(SELECT_PERMISSIONS)
                    .param("adventureId", adventureId)
                    .query((r, __) -> new PermissionDto(UUID.fromString(r.getString("public_id")),
                            PermissionLevel.valueOf(r.getString("permission"))))
                    .list());

            var modelConfiguration = new ModelConfigurationDto(
                    ArtificialIntelligenceModel.fromString(rs.getString("ai_model")),
                    rs.getInt("max_token_limit"),
                    rs.getDouble("temperature"));

            var contextAttributes = new ContextAttributesDto(
                    rs.getString("nudge"),
                    rs.getString("authors_note"),
                    rs.getString("scene"),
                    rs.getString("bump"),
                    rs.getObject("bump_frequency", Integer.class));

            var lorebook = new HashSet<>(jdbcClient.sql(SELECT_LOREBOOK)
                    .param("adventureId", adventureId)
                    .query((r, __) -> new AdventureLorebookEntryDetails(
                            UUID.fromString(r.getString("public_id")),
                            UUID.fromString(rs.getString("public_id")),
                            r.getString("name"),
                            r.getString("description"),
                            r.getString("player_id"),
                            r.getBoolean("is_player_character"),
                            r.getTimestamp("creation_date").toInstant(),
                            r.getTimestamp("last_update_date").toInstant()))
                    .list());

            return new AdventureDetails(
                    UUID.fromString(rs.getString("public_id")),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("adventure_start"),
                    rs.getObject("world_id", UUID.class),
                    rs.getString("narrator_name"),
                    rs.getString("narrator_personality"),
                    Visibility.valueOf(rs.getString("visibility")),
                    Moderation.valueOf(rs.getString("moderation")),
                    rs.getBoolean("is_multiplayer"),
                    storagePort.resolveUrl(rs.getString("image_key")),
                    rs.getTimestamp("creation_date").toInstant(),
                    rs.getTimestamp("last_update_date").toInstant(),
                    modelConfiguration,
                    contextAttributes,
                    permissions,
                    lorebook);
        };
    }
}
