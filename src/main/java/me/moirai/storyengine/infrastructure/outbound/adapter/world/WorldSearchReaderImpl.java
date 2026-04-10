package me.moirai.storyengine.infrastructure.outbound.adapter.world;

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
import me.moirai.storyengine.core.port.inbound.world.SearchWorlds;
import me.moirai.storyengine.core.port.inbound.world.WorldSortField;
import me.moirai.storyengine.core.port.outbound.world.WorldSearchReader;
import me.moirai.storyengine.core.port.outbound.world.WorldSearchRow;

@Repository
public class WorldSearchReaderImpl implements WorldSearchReader {

    //@formatter:off
    private static final String SELECT_SQL = """
            SELECT w.public_id,
                   w.name,
                   w.description,
                   w.visibility,
                   w.creation_date,
                   wp_me.permission AS user_permission
              FROM world w
              LEFT JOIN world_permissions wp_me ON wp_me.world_id = w.id AND wp_me.user_id = :requesterId
            """;
    //@formatter:on

    private final JdbcClient jdbcClient;

    public WorldSearchReaderImpl(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public PaginatedResult<WorldSearchRow> search(SearchWorlds query) {

        var pq = PaginatedQuery.builder()
                .select(SELECT_SQL)
                .filter(Optional.of(resolveView(query.view(), query.requesterId())))
                .filter(Filters.containsIgnoreCase("w.name", "name", query.name()))
                .sortBy(resolveSortingField(query.sortingField()), query.direction())
                .page(query.page(), query.size())
                .build();

        var params = pq.parameters();
        params.put("requesterId", query.requesterId());

        var countParams = pq.countParameters();
        countParams.put("requesterId", query.requesterId());

        var data = jdbcClient.sql(pq.sql())
                .params(params)
                .query(toWorldSearchRow())
                .list();

        var totalItems = jdbcClient.sql(pq.countSql())
                .params(countParams)
                .query(Long.class)
                .single();

        return PaginatedResult.of(data, totalItems, pq.page(), pq.size());
    }

    private Filter resolveView(SearchView view, Long requesterId) {
        return switch (view) {
            case MY_STUFF -> new Filter(
                    "EXISTS (SELECT 1 FROM world_permissions wp WHERE wp.world_id = w.id AND wp.user_id = :requesterId)",
                    "requesterId", requesterId);
            case SHARED_WITH_ME -> new Filter(
                    "NOT EXISTS (SELECT 1 FROM world_permissions wp WHERE wp.world_id = w.id AND wp.user_id = :requesterId AND wp.permission = 'OWNER') AND EXISTS (SELECT 1 FROM world_permissions wp WHERE wp.world_id = w.id AND wp.user_id = :requesterId)",
                    "requesterId", requesterId);
            case EXPLORE -> new Filter("w.visibility = 'PUBLIC'", null, null);
        };
    }

    private String resolveSortingField(WorldSortField field) {
        return switch (field) {
            case NAME -> "w.name";
            case LAST_UPDATE_DATE -> "w.last_update_date";
            case null, default -> "w.creation_date";
        };
    }

    private RowMapper<WorldSearchRow> toWorldSearchRow() {
        return (rs, _) -> new WorldSearchRow(
                UUID.fromString(rs.getString("public_id")),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("visibility"),
                rs.getTimestamp("creation_date").toInstant(),
                rs.getString("user_permission"));
    }
}
