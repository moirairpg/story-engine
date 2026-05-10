package me.moirai.storyengine.infrastructure.outbound.adapter.character;

import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import me.moirai.storyengine.common.enums.CharacterClass;
import me.moirai.storyengine.common.util.Functions;
import me.moirai.storyengine.core.port.outbound.character.PlayerCharacterDetailsRow;
import me.moirai.storyengine.core.port.outbound.character.PlayerCharacterReader;

@Repository
public class PlayerCharacterReaderImpl implements PlayerCharacterReader {

    private static final String SELECT_BY_PUBLIC_ID = """
            SELECT  pc.public_id,
                    owner.username AS owner_username,
                    pc.name,
                    pc.character_class,
                    pc.personality,
                    pc.physical_description,
                    pc.image_key,
                    pc.creation_date,
                    pc.last_update_date
               FROM player_character pc
               JOIN moirai_user owner ON owner.id = pc.player_id
              WHERE pc.public_id = :characterId
            """;

    private static final String SELECT_OWNER_USERNAME = """
            SELECT owner.username AS owner_username
              FROM player_character pc
              JOIN moirai_user owner ON owner.id = pc.player_id
             WHERE pc.public_id = :characterId
            """;

    private final JdbcClient jdbcClient;

    public PlayerCharacterReaderImpl(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Optional<PlayerCharacterDetailsRow> getById(UUID characterId) {

        return jdbcClient.sql(SELECT_BY_PUBLIC_ID)
                .param("characterId", characterId)
                .query(toPlayerCharacterDetailsRow())
                .optional();
    }

    @Override
    public Optional<String> getOwnerUsername(UUID characterId) {

        return jdbcClient.sql(SELECT_OWNER_USERNAME)
                .param("characterId", characterId)
                .query((rs, _) -> rs.getString("owner_username"))
                .optional();
    }

    private RowMapper<PlayerCharacterDetailsRow> toPlayerCharacterDetailsRow() {

        return (rs, _) -> new PlayerCharacterDetailsRow(
                rs.getObject("public_id", UUID.class),
                rs.getString("owner_username"),
                rs.getString("name"),
                Functions.mapOrNull(rs.getString("character_class"), CharacterClass::valueOf),
                rs.getString("personality"),
                rs.getString("physical_description"),
                rs.getString("image_key"),
                rs.getTimestamp("creation_date").toInstant(),
                rs.getTimestamp("last_update_date").toInstant());
    }
}
