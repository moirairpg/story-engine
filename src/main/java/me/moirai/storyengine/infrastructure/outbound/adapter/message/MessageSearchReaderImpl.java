package me.moirai.storyengine.infrastructure.outbound.adapter.message;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import me.moirai.storyengine.common.dbutil.Filter;
import me.moirai.storyengine.common.dbutil.Filters;
import me.moirai.storyengine.common.dbutil.PaginatedQuery;
import me.moirai.storyengine.common.dto.PaginatedResult;
import me.moirai.storyengine.common.enums.AiRole;
import me.moirai.storyengine.common.enums.SortDirection;
import me.moirai.storyengine.core.domain.message.MessageStatus;
import me.moirai.storyengine.core.port.inbound.adventure.MessageSummary;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventureMessages;
import me.moirai.storyengine.core.port.outbound.message.MessageSearchReader;

@Repository
public class MessageSearchReaderImpl implements MessageSearchReader {

    //@formatter:off
    private static final String SELECT_SQL = """
            SELECT m.public_id,
                   m.role,
                   m.content,
                   m.status,
                   m.creation_date
              FROM message m
              JOIN adventure a ON m.adventure_id = a.id
            """;
    //@formatter:on

    private final JdbcClient jdbcClient;

    public MessageSearchReaderImpl(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public PaginatedResult<MessageSummary> search(SearchAdventureMessages query) {

        var pq = PaginatedQuery.builder()
                .select(SELECT_SQL)
                .filter(Optional.of(new Filter("a.public_id = :adventurePublicId", "adventurePublicId", query.adventureId())))
                .filter(Filters.in("m.status", "status", List.of(MessageStatus.ACTIVE.name(), MessageStatus.CHRONICLED.name())))
                .sortBy("m.creation_date", SortDirection.DESC)
                .page(query.page(), query.size())
                .build();

        var data = jdbcClient.sql(pq.sql())
                .params(pq.parameters())
                .query(toMessageSummary())
                .list();

        var totalItems = jdbcClient.sql(pq.countSql())
                .params(pq.countParameters())
                .query(Long.class)
                .single();

        return PaginatedResult.of(data, totalItems, pq.page(), pq.size());
    }

    private RowMapper<MessageSummary> toMessageSummary() {
        return (rs, _) -> new MessageSummary(
                UUID.fromString(rs.getString("public_id")),
                AiRole.valueOf(rs.getString("role")),
                rs.getString("content"),
                MessageStatus.valueOf(rs.getString("status")),
                rs.getTimestamp("creation_date").toInstant());
    }
}
