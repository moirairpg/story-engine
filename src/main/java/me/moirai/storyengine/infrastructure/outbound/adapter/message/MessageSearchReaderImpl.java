package me.moirai.storyengine.infrastructure.outbound.adapter.message;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import me.moirai.storyengine.common.dbutil.Filter;
import me.moirai.storyengine.common.dbutil.Filters;
import me.moirai.storyengine.common.dbutil.QueryBuilder;
import me.moirai.storyengine.common.dto.CursorResult;
import me.moirai.storyengine.common.enums.MessageAuthorRole;
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
                   m.created_by,
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
    public CursorResult<MessageSummary> search(SearchAdventureMessages query) {

        var builtQuery = QueryBuilder.select(SELECT_SQL)
                .filter(Filters.equals("a.public_id", "adventurePublicId", query.adventureId()))
                .filter(Filters.in("m.status", "status",
                        List.of(MessageStatus.ACTIVE.name(), MessageStatus.CHRONICLED.name())))
                .filter(cursorFilter(query.lastMessageId()))
                .sortBy("m.id", SortDirection.DESC)
                .limit(query.size())
                .build();

        var data = jdbcClient.sql(builtQuery.sql())
                .params(builtQuery.parameters())
                .query(toMessageSummary())
                .list();

        return CursorResult.of(data, query.size());
    }

    private RowMapper<MessageSummary> toMessageSummary() {
        return (rs, _) -> new MessageSummary(
                UUID.fromString(rs.getString("public_id")),
                MessageAuthorRole.valueOf(rs.getString("role")),
                rs.getString("content"),
                MessageStatus.valueOf(rs.getString("status")),
                rs.getString("created_by"),
                rs.getTimestamp("creation_date").toInstant());
    }

    private static Optional<Filter> cursorFilter(UUID lastMessageId) {

        if (lastMessageId == null) {
            return Optional.empty();
        }

        return Optional.of(new Filter(
                "m.id < (SELECT id FROM message WHERE public_id = :lastMessageId)",
                "lastMessageId",
                lastMessageId));
    }
}
