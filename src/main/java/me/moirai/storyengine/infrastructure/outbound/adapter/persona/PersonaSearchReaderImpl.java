package me.moirai.storyengine.infrastructure.outbound.adapter.persona;

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
import me.moirai.storyengine.core.port.inbound.persona.PersonaSortField;
import me.moirai.storyengine.core.port.inbound.persona.PersonaSummary;
import me.moirai.storyengine.core.port.inbound.persona.SearchPersonas;
import me.moirai.storyengine.core.port.outbound.persona.PersonaSearchReader;

@Repository
public class PersonaSearchReaderImpl implements PersonaSearchReader {

    //@formatter:off
    private static final String SELECT_SQL = """
            SELECT p.public_id,
                   p.name,
                   p.personality,
                   p.visibility,
                   p.creation_date
              FROM persona p
            """;
    //@formatter:on

    private final JdbcClient jdbcClient;

    public PersonaSearchReaderImpl(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public PaginatedResult<PersonaSummary> search(SearchPersonas query) {

        var pq = PaginatedQuery.builder()
                .select(SELECT_SQL)
                .filter(Optional.of(resolveView(query.view(), query.requesterId())))
                .filter(Filters.containsIgnoreCase("p.name", "name", query.name()))
                .filter(Filters.equals("p.owner_id", "ownerId", query.ownerId()))
                .sortBy(resolveSortingField(query.sortingField()), query.direction())
                .page(query.page(), query.size())
                .build();

        var data = jdbcClient.sql(pq.sql())
                .params(pq.parameters())
                .query(toPersonaSummary())
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
                    "(p.owner_id = :requesterId OR p.users_allowed_to_write LIKE '%' || :requesterId || '%')",
                    "requesterId", requesterId);
            case SHARED_WITH_ME -> new Filter(
                    "p.owner_id != :requesterId AND (p.users_allowed_to_read LIKE '%' || :requesterId || '%' OR p.users_allowed_to_write LIKE '%' || :requesterId || '%')",
                    "requesterId", requesterId);
            case EXPLORE -> new Filter("p.visibility = 'PUBLIC'", null, null);
        };
    }

    private String resolveSortingField(PersonaSortField field) {
        return switch (field) {
            case NAME -> "p.name";
            case LAST_UPDATE_DATE -> "p.last_update_date";
            case null, default -> "p.creation_date";
        };
    }

    private RowMapper<PersonaSummary> toPersonaSummary() {
        return (rs, _) -> new PersonaSummary(
                UUID.fromString(rs.getString("public_id")),
                rs.getString("name"),
                rs.getString("personality"),
                rs.getString("visibility"),
                rs.getObject("creation_date", OffsetDateTime.class));
    }
}
