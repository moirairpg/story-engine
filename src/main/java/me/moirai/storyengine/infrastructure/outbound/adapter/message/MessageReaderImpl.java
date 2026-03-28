package me.moirai.storyengine.infrastructure.outbound.adapter.message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import me.moirai.storyengine.common.enums.AiRole;
import me.moirai.storyengine.core.domain.message.MessageStatus;
import me.moirai.storyengine.core.port.outbound.message.MessageData;
import me.moirai.storyengine.core.port.outbound.message.MessageReader;

@Repository
public class MessageReaderImpl implements MessageReader {

    //@formatter:off
    private static final String FIND_ACTIVE_BY_ADVENTURE = """
            SELECT m.public_id,
                   m.adventure_id,
                   m.created_by,
                   m.role,
                   m.content,
                   m.creation_date,
                   m.status
              FROM message m
             WHERE m.adventure_id = :adventureId
               AND m.status = 'ACTIVE'
             ORDER BY m.creation_date DESC
             LIMIT :limit
            """;
    //@formatter:on

    private final JdbcClient jdbcClient;

    public MessageReaderImpl(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public List<MessageData> findActiveByAdventureId(Long adventureId, int limit) {

        var results = jdbcClient.sql(FIND_ACTIVE_BY_ADVENTURE)
                .param("adventureId", adventureId)
                .param("limit", limit)
                .query(toMessageData())
                .list();

        var reversed = new ArrayList<>(results);
        Collections.reverse(reversed);

        return Collections.unmodifiableList(reversed);
    }

    private RowMapper<MessageData> toMessageData() {
        return (rs, _) -> new MessageData(
                UUID.fromString(rs.getString("public_id")),
                rs.getLong("adventure_id"),
                rs.getString("created_by"),
                AiRole.valueOf(rs.getString("role")),
                rs.getString("content"),
                rs.getTimestamp("creation_date").toInstant(),
                MessageStatus.valueOf(rs.getString("status")));
    }
}
