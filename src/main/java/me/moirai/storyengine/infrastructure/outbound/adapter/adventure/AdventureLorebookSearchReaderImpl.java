package me.moirai.storyengine.infrastructure.outbound.adapter.adventure;

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
import me.moirai.storyengine.core.port.inbound.adventure.AdventureLorebookSortField;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventureLorebookEntries;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureLorebookSearchReader;

@Repository
public class AdventureLorebookSearchReaderImpl implements AdventureLorebookSearchReader {

    //@formatter:off
    private static final String SELECT_SQL = """
            SELECT al.public_id,
                   al.name,
                   al.description,
                   al.creation_date
              FROM adventure_lorebook al
              JOIN adventure a ON al.adventure_id = a.id
            """;
    //@formatter:on

    private final JdbcClient jdbcClient;

    public AdventureLorebookSearchReaderImpl(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public PaginatedResult<LorebookEntrySummary> search(SearchAdventureLorebookEntries query) {

        var pq = PaginatedQuery.builder()
                .select(SELECT_SQL)
                .filter(Optional.of(new Filter("a.public_id = :adventurePublicId", "adventurePublicId", query.adventureId())))
                .filter(Filters.containsIgnoreCase("al.name", "name", query.name()))
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

    private String resolveSortingField(AdventureLorebookSortField field) {
        return switch (field) {
            case NAME -> "al.name";
            case LAST_UPDATE_DATE -> "al.last_update_date";
            case null, default -> "al.creation_date";
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
