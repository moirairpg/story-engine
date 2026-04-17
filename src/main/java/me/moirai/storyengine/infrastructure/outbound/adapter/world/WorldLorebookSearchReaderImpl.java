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
import me.moirai.storyengine.core.port.inbound.LorebookEntrySummary;
import me.moirai.storyengine.core.port.inbound.world.SearchWorldLorebookEntries;
import me.moirai.storyengine.core.port.inbound.world.WorldLorebookSortField;
import me.moirai.storyengine.core.port.outbound.world.WorldLorebookSearchReader;

@Repository
public class WorldLorebookSearchReaderImpl implements WorldLorebookSearchReader {

    //@formatter:off
    private static final String SELECT_SQL = """
            SELECT wl.public_id,
                   wl.name,
                   wl.description,
                   wl.creation_date
              FROM world_lorebook wl
              JOIN world w ON wl.world_id = w.id
            """;
    //@formatter:on

    private final JdbcClient jdbcClient;

    public WorldLorebookSearchReaderImpl(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public PaginatedResult<LorebookEntrySummary> search(SearchWorldLorebookEntries query) {

        var pq = PaginatedQuery.builder()
                .select(SELECT_SQL)
                .filter(Optional.of(new Filter("w.public_id = :worldPublicId", "worldPublicId", query.worldId())))
                .filter(Filters.containsIgnoreCase("wl.name", "name", query.name()))
                .sortBy(resolveSortingField(query.sortingField()), query.direction())
                .page(query.page(), query.size())
                .build();

        var data = jdbcClient.sql(pq.sql())
                .params(pq.parameters())
                .query(toLorebookEntrySummary())
                .list();

        var totalItems = jdbcClient.sql(pq.countSql())
                .params(pq.countParameters())
                .query(Long.class)
                .single();

        return PaginatedResult.of(data, totalItems, pq.page(), pq.size());
    }

    private String resolveSortingField(WorldLorebookSortField field) {
        return switch (field) {
            case NAME -> "wl.name";
            case LAST_UPDATE_DATE -> "wl.last_update_date";
            case null, default -> "wl.creation_date";
        };
    }

    private RowMapper<LorebookEntrySummary> toLorebookEntrySummary() {
        return (rs, _) -> new LorebookEntrySummary(
                UUID.fromString(rs.getString("public_id")),
                rs.getString("name"),
                rs.getString("description"),
                rs.getTimestamp("creation_date").toInstant());
    }
}
