package me.moirai.storyengine.infrastructure.outbound.adapter.adventure;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import me.moirai.storyengine.common.enums.CharacterClass;
import me.moirai.storyengine.common.util.Functions;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRosterReader;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureMembershipSummaryRow;
import me.moirai.storyengine.core.port.outbound.adventure.CharacterAdventureSummaryRow;

@Repository
public class AdventureRosterReaderImpl implements AdventureRosterReader {

    //@formatter:off
    private static final String SELECT_ADVENTURES_BY_CHARACTER = """
            SELECT a.public_id           AS adventure_public_id,
                   a.name                AS adventure_name,
                   a.image_key           AS adventure_image_key,
                   a.ui_image_position_x AS adventure_ui_image_position_x,
                   a.ui_image_position_y AS adventure_ui_image_position_y
              FROM adventure_membership am
                   JOIN adventure a         ON a.id  = am.adventure_id
                   JOIN player_character pc ON pc.id = am.player_character_id
             WHERE pc.public_id = :characterPublicId
            """;

    private static final String SELECT_BY_ADVENTURE = """
            SELECT pc.public_id            AS player_character_public_id,
                   u.public_id             AS player_public_id,
                   u.username              AS player_username,
                   u.display_name          AS player_display_name,
                   pc.name                 AS name,
                   pc.character_class      AS character_class,
                   pc.image_key            AS image_key,
                   pc.ui_image_position_x  AS ui_image_position_x,
                   pc.ui_image_position_y  AS ui_image_position_y
              FROM adventure_membership am
                   JOIN adventure a          ON a.id  = am.adventure_id
                   JOIN player_character pc  ON pc.id = am.player_character_id
                   JOIN moirai_user u        ON u.id  = pc.player_id
             WHERE a.public_id = :adventurePublicId
            """;
    //@formatter:on

    private final JdbcClient jdbcClient;

    public AdventureRosterReaderImpl(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public List<CharacterAdventureSummaryRow> getAdventuresByPlayerCharacterPublicId(UUID characterPublicId) {

        return jdbcClient.sql(SELECT_ADVENTURES_BY_CHARACTER)
                .param("characterPublicId", characterPublicId)
                .query(toCharacterAdventureSummaryRow())
                .list();
    }

    @Override
    public List<AdventureMembershipSummaryRow> getAllByAdventurePublicId(UUID adventurePublicId) {

        return jdbcClient.sql(SELECT_BY_ADVENTURE)
                .param("adventurePublicId", adventurePublicId)
                .query(toAdventureMembershipSummaryRow())
                .list();
    }

    private RowMapper<CharacterAdventureSummaryRow> toCharacterAdventureSummaryRow() {

        return (rs, _) -> new CharacterAdventureSummaryRow(
                rs.getObject("adventure_public_id", UUID.class),
                rs.getString("adventure_name"),
                rs.getString("adventure_image_key"),
                Functions.mapOrNull(rs.getBigDecimal("adventure_ui_image_position_x"), BigDecimal::doubleValue),
                Functions.mapOrNull(rs.getBigDecimal("adventure_ui_image_position_y"), BigDecimal::doubleValue));
    }

    private RowMapper<AdventureMembershipSummaryRow> toAdventureMembershipSummaryRow() {

        return (rs, _) -> new AdventureMembershipSummaryRow(
                rs.getObject("player_character_public_id", UUID.class),
                rs.getObject("player_public_id", UUID.class),
                rs.getString("player_username"),
                rs.getString("player_display_name"),
                rs.getString("name"),
                Functions.mapOrNull(rs.getString("character_class"), CharacterClass::valueOf),
                rs.getString("image_key"),
                Functions.mapOrNull(rs.getBigDecimal("ui_image_position_x"), BigDecimal::doubleValue),
                Functions.mapOrNull(rs.getBigDecimal("ui_image_position_y"), BigDecimal::doubleValue));
    }
}
