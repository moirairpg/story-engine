package me.moirai.storyengine.infrastructure.outbound.adapter.character;

import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import me.moirai.storyengine.common.dbutil.Filters;
import me.moirai.storyengine.common.dbutil.PaginatedQuery;
import me.moirai.storyengine.common.dto.PaginatedResult;
import me.moirai.storyengine.common.enums.CharacterClass;
import me.moirai.storyengine.common.enums.PlayerCharacterSortField;
import me.moirai.storyengine.common.util.Functions;
import me.moirai.storyengine.core.port.inbound.character.SearchPlayerCharacters;
import me.moirai.storyengine.core.port.outbound.character.PlayerCharacterSearchReader;
import me.moirai.storyengine.core.port.outbound.character.PlayerCharacterSummaryRow;

@Repository
public class PlayerCharacterSearchReaderImpl implements PlayerCharacterSearchReader {

    private static final String SELECT_SQL = """
            SELECT  pc.public_id,
                    owner.username AS owner_username,
                    pc.name,
                    pc.character_class,
                    pc.image_key
               FROM player_character pc
               JOIN moirai_user owner ON owner.id = pc.player_id
            """;

    private final JdbcClient jdbcClient;

    public PlayerCharacterSearchReaderImpl(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public PaginatedResult<PlayerCharacterSummaryRow> search(SearchPlayerCharacters query) {

        var pq = PaginatedQuery.builder()
                .select(SELECT_SQL)
                .filter(Filters.equals("pc.player_id", "requesterId", query.requesterId()))
                .filter(Filters.containsIgnoreCase("pc.name", "name", query.name()))
                .filter(Filters.equals("pc.character_class", "characterClass",
                        Functions.mapOrNull(query.characterClass(), CharacterClass::name)))
                .sortBy(resolveSortingField(query.sortingField()), query.direction())
                .page(query.page(), query.size())
                .build();

        var params = pq.parameters();
        params.put("requesterId", query.requesterId());

        var countParams = pq.countParameters();
        countParams.put("requesterId", query.requesterId());

        var data = jdbcClient.sql(pq.sql())
                .params(params)
                .query(toPlayerCharacterSummaryRow())
                .list();

        var totalItems = jdbcClient.sql(pq.countSql())
                .params(countParams)
                .query(Long.class)
                .single();

        return PaginatedResult.of(data, totalItems, pq.page(), pq.size());
    }

    private String resolveSortingField(PlayerCharacterSortField field) {

        if (field == null) {
            return "pc.creation_date";
        }

        return switch (field) {
            case NAME -> "pc.name";
            case CREATION_DATE -> "pc.creation_date";
            case LAST_UPDATE_DATE -> "pc.last_update_date";
        };
    }

    private RowMapper<PlayerCharacterSummaryRow> toPlayerCharacterSummaryRow() {

        return (rs, _) -> new PlayerCharacterSummaryRow(
                rs.getObject("public_id", UUID.class),
                rs.getString("owner_username"),
                rs.getString("name"),
                Functions.mapOrNull(rs.getString("character_class"), CharacterClass::valueOf),
                rs.getString("image_key"));
    }
}