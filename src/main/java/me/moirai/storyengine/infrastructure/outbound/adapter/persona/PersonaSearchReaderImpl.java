package me.moirai.storyengine.infrastructure.outbound.adapter.persona;

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
import me.moirai.storyengine.core.port.inbound.persona.SearchPersonas;
import me.moirai.storyengine.core.port.outbound.persona.PersonaSearchReader;
import me.moirai.storyengine.core.port.outbound.persona.PersonaSearchRow;

@Repository
public class PersonaSearchReaderImpl implements PersonaSearchReader {

    //@formatter:off
    private static final String SELECT_SQL = """
            SELECT p.public_id,
                   p.name,
                   p.personality,
                   p.visibility,
                   p.creation_date,
                   pp_me.permission AS user_permission
              FROM persona p
              LEFT JOIN persona_permissions pp_me ON pp_me.persona_id = p.id AND pp_me.user_id = :requesterId
            """;
    //@formatter:on

    private final JdbcClient jdbcClient;

    public PersonaSearchReaderImpl(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public PaginatedResult<PersonaSearchRow> search(SearchPersonas query) {

        var pq = PaginatedQuery.builder()
                .select(SELECT_SQL)
                .filter(Optional.of(resolveView(query.view(), query.requesterId())))
                .filter(Filters.containsIgnoreCase("p.name", "name", query.name()))
                .sortBy(resolveSortingField(query.sortingField()), query.direction())
                .page(query.page(), query.size())
                .build();

        var params = pq.parameters();
        params.put("requesterId", query.requesterId());

        var countParams = pq.countParameters();
        countParams.put("requesterId", query.requesterId());

        var data = jdbcClient.sql(pq.sql())
                .params(params)
                .query(toPersonaSearchRow())
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
                    "EXISTS (SELECT 1 FROM persona_permissions pp WHERE pp.persona_id = p.id AND pp.user_id = :requesterId)",
                    "requesterId", requesterId);
            case SHARED_WITH_ME -> new Filter(
                    "NOT EXISTS (SELECT 1 FROM persona_permissions pp WHERE pp.persona_id = p.id AND pp.user_id = :requesterId AND pp.permission = 'OWNER') AND EXISTS (SELECT 1 FROM persona_permissions pp WHERE pp.persona_id = p.id AND pp.user_id = :requesterId)",
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

    private RowMapper<PersonaSearchRow> toPersonaSearchRow() {
        return (rs, _) -> new PersonaSearchRow(
                UUID.fromString(rs.getString("public_id")),
                rs.getString("name"),
                rs.getString("personality"),
                rs.getString("visibility"),
                rs.getTimestamp("creation_date").toInstant(),
                rs.getString("user_permission"));
    }
}
