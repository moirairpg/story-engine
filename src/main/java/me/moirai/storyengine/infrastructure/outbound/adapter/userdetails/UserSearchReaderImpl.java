package me.moirai.storyengine.infrastructure.outbound.adapter.userdetails;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import me.moirai.storyengine.common.dbutil.Filters;
import me.moirai.storyengine.common.dbutil.PaginatedQuery;
import me.moirai.storyengine.common.dto.PaginatedResult;
import me.moirai.storyengine.common.enums.Role;
import me.moirai.storyengine.common.util.Functions;
import me.moirai.storyengine.core.port.inbound.userdetails.SearchUsers;
import me.moirai.storyengine.core.port.inbound.userdetails.UserSortField;
import me.moirai.storyengine.core.port.inbound.userdetails.UserSummary;
import me.moirai.storyengine.core.port.outbound.userdetails.UserSearchReader;

@Repository
public class UserSearchReaderImpl implements UserSearchReader {

    //@formatter:off
    private static final String SELECT_SQL = """
            SELECT u.public_id,
                   u.username,
                   u.role,
                   u.is_active,
                   u.creation_date
              FROM moirai_user u
            """;
    //@formatter:on

    private final JdbcClient jdbcClient;

    public UserSearchReaderImpl(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public PaginatedResult<UserSummary> search(SearchUsers query) {

        var paginatedQuery = PaginatedQuery.builder()
                .select(SELECT_SQL)
                .filter(Filters.containsIgnoreCase("u.username", "username", query.username()))
                .filter(Filters.equals("u.role", "role", Functions.mapOrNull(query.role(), Role::name)))
                .filter(Filters.equals("u.is_active", "isActive", query.isActive()))
                .filter(Filters.greaterOrEqualThan("u.creation_date", "registeredFrom",
                        toOffsetDateTime(query.registeredFrom())))
                .filter(Filters.lowerOrEqualThan("u.creation_date", "registeredTo",
                        toOffsetDateTime(query.registeredTo())))
                .sortBy(resolveSortField(query.sortingField()), query.direction())
                .page(query.page(), query.size())
                .build();

        var data = jdbcClient.sql(paginatedQuery.sql())
                .params(paginatedQuery.parameters())
                .query((rs, _) -> new UserSummary(
                        UUID.fromString(rs.getString("public_id")),
                        rs.getString("username"),
                        Role.valueOf(rs.getString("role")),
                        rs.getBoolean("is_active"),
                        rs.getTimestamp("creation_date").toInstant()))
                .list();

        var totalItems = jdbcClient.sql(paginatedQuery.countSql())
                .params(paginatedQuery.countParameters())
                .query(Long.class)
                .single();

        return PaginatedResult.of(data, totalItems, paginatedQuery.page(), paginatedQuery.size());
    }

    private OffsetDateTime toOffsetDateTime(Instant value) {
        return Functions.mapOrNull(value, instant -> instant.atOffset(ZoneOffset.UTC));
    }

    private String resolveSortField(UserSortField field) {
        return switch (field) {
            case USERNAME -> "u.username";
            case ROLE -> "u.role";
            case CREATION_DATE -> "u.creation_date";
            case null, default -> "u.creation_date";
        };
    }
}
