package me.moirai.storyengine.infrastructure.outbound.adapter.adventure;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import me.moirai.storyengine.common.dbutil.Filter;
import me.moirai.storyengine.common.dbutil.Filters;
import me.moirai.storyengine.common.dbutil.PaginatedQuery;
import me.moirai.storyengine.common.dto.PaginatedResult;
import me.moirai.storyengine.common.enums.SearchView;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureSortField;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureSummary;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventures;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureSearchReader;

@Repository
public class AdventureSearchReaderImpl implements AdventureSearchReader {

    //@formatter:off
    private static final String SELECT_SQL = """
            SELECT  a.public_id,
                    a.name,
                    a.description,
                    w.name    AS world_name,
                    p.name    AS persona_name,
                    a.visibility,
                    a.creation_date
               FROM adventure a
               JOIN world   w ON a.world_id   = w.id
               JOIN persona p ON a.persona_id = p.id
            """;
    //@formatter:on

    private final JdbcClient jdbcClient;

    public AdventureSearchReaderImpl(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public PaginatedResult<AdventureSummary> search(SearchAdventures query) {

        var pq = PaginatedQuery.builder()
                .select(SELECT_SQL)
                .filter(Optional.of(resolveView(query.view(), query.requesterId())))
                .filter(Filters.containsIgnoreCase("a.name", "name", query.name()))
                .filter(Filters.containsIgnoreCase("w.name", "worldName", query.worldName()))
                .filter(Filters.containsIgnoreCase("p.name", "personaName", query.personaName()))
                .filter(Filters.equals("a.owner_id", "ownerId", query.ownerId()))
                .filter(Filters.equals("a.ai_model", "model", query.model()))
                .filter(Filters.equals("a.game_mode", "gameMode", query.gameMode()))
                .filter(Filters.equals("a.moderation", "moderation", query.moderation()))
                .filter(Filters.equals("a.is_multiplayer", "isMultiplayer", query.isMultiplayer()))
                .sortBy(resolveSortingField(query.sortingField()), query.direction())
                .page(query.page(), query.size())
                .build();

        var data = jdbcClient.sql(pq.sql())
                .params(pq.parameters())
                .query(toAdventureSummary())
                .list();

        var totalItems = jdbcClient.sql(pq.countSql())
                .params(pq.countParameters())
                .query(Long.class)
                .single();

        return PaginatedResult.of(data, totalItems, pq.page(), pq.size());
    }

    private Filter resolveView(SearchView view, String requesterId) {
        return switch (view) {
            case MY_STUFF -> new Filter(
                    "(a.owner_id = :requesterId OR a.users_allowed_to_write LIKE '%' || :requesterId || '%')",
                    "requesterId", requesterId);
            case SHARED_WITH_ME -> new Filter(
                    "a.owner_id != :requesterId AND (a.users_allowed_to_read LIKE '%' || :requesterId || '%' OR a.users_allowed_to_write LIKE '%' || :requesterId || '%')",
                    "requesterId", requesterId);
            case EXPLORE -> new Filter("a.visibility = 'PUBLIC'", null, null);
        };
    }

    private String resolveSortingField(AdventureSortField field) {
        return switch (field) {
            case NAME -> "a.name";
            case GAME_MODE -> "a.game_mode";
            case MODEL -> "a.ai_model";
            case MODERATION -> "a.moderation";
            case LAST_UPDATE_DATE -> "a.last_update_date";
            case null, default -> "a.creation_date";
        };
    }

    private RowMapper<AdventureSummary> toAdventureSummary() {
        return (rs, _) -> new AdventureSummary(
                UUID.fromString(rs.getString("public_id")),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("world_name"),
                rs.getString("persona_name"),
                rs.getString("visibility"),
                rs.getObject("creation_date", OffsetDateTime.class));
    }
}
