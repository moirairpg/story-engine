package me.moirai.storyengine.infrastructure.outbound.adapter.world;

import java.time.Instant;
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
import me.moirai.storyengine.core.port.inbound.world.WorldSummary;
import me.moirai.storyengine.core.port.outbound.world.WorldSearchReader;

@Repository
public class WorldSearchReaderImpl implements WorldSearchReader {

    //@formatter:off
    private static final String SELECT_SQL = """
            SELECT w.public_id,
                   w.name,
                   w.description,
                   w.visibility,
                   w.creation_date
              FROM world w
            """;
    //@formatter:on

    private final JdbcClient jdbcClient;

    public WorldSearchReaderImpl(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public PaginatedResult<WorldSummary> search(SearchWorlds query) {

        var pq = PaginatedQuery.builder()
                .select(SELECT_SQL)
                .filter(Optional.of(resolveView(query.view(), query.requesterId())))
                .filter(Filters.containsIgnoreCase("w.name", "name", query.name()))
                .filter(Filters.equals("w.owner_id", "ownerId", query.ownerId()))
                .sortBy(resolveSortingField(query.sortingField()), query.direction())
                .page(query.page(), query.size())
                .build();

        var data = jdbcClient.sql(pq.sql())
                .params(pq.parameters())
                .query(toWorldSummary())
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
                    "(w.owner_id = :requesterId OR w.users_allowed_to_write LIKE '%' || :requesterId || '%')",
                    "requesterId", requesterId);
            case SHARED_WITH_ME -> new Filter(
                    "w.owner_id != :requesterId AND (w.users_allowed_to_read LIKE '%' || :requesterId || '%' OR w.users_allowed_to_write LIKE '%' || :requesterId || '%')",
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

    private RowMapper<WorldSummary> toWorldSummary() {
        return (rs, _) -> new WorldSummary(
                UUID.fromString(rs.getString("public_id")),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("visibility"),
                rs.getTimestamp("creation_date").toInstant());
    }
}
