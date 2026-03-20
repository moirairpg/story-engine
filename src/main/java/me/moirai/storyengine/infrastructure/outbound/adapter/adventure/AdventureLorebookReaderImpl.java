package me.moirai.storyengine.infrastructure.outbound.adapter.adventure;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import me.moirai.storyengine.core.port.inbound.adventure.AdventureLorebookEntryDetails;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureLorebookReader;

@Repository
public class AdventureLorebookReaderImpl implements AdventureLorebookReader {

    //@formatter:off
    private static final String SELECT_BY_ID = """
            SELECT al.public_id,
                    a.public_id AS adventure_public_id,
                   al.name,
                   al.description,
                   al.regex,
                   al.player_id,
                   al.is_player_character,
                   al.creation_date,
                   al.last_update_date
              FROM adventure_lorebook al
              JOIN adventure a ON al.adventure_id = a.id
             WHERE al.public_id = :entryPublicId
               AND  a.public_id = :adventurePublicId
            """;
    //@formatter:on

    private final JdbcClient jdbcClient;

    public AdventureLorebookReaderImpl(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Optional<AdventureLorebookEntryDetails> getAdventureLorebookEntryById(UUID entryPublicId, UUID adventurePublicId) {
        return jdbcClient.sql(SELECT_BY_ID)
                .param("entryPublicId", entryPublicId)
                .param("adventurePublicId", adventurePublicId)
                .query(toAdventureLorebookEntryDetails())
                .optional();
    }

    private RowMapper<AdventureLorebookEntryDetails> toAdventureLorebookEntryDetails() {
        return (rs, _) -> new AdventureLorebookEntryDetails(
                UUID.fromString(rs.getString("public_id")),
                UUID.fromString(rs.getString("adventure_public_id")),
                rs.getString("name"),
                rs.getString("regex"),
                rs.getString("description"),
                rs.getString("player_id"),
                rs.getBoolean("is_player_character"),
                rs.getObject("creation_date", OffsetDateTime.class),
                rs.getObject("last_update_date", OffsetDateTime.class));
    }
}
