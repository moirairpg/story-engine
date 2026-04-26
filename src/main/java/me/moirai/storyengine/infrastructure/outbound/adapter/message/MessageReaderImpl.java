package me.moirai.storyengine.infrastructure.outbound.adapter.message;

import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import me.moirai.storyengine.common.enums.MessageAuthorRole;
import me.moirai.storyengine.core.domain.message.MessageStatus;
import me.moirai.storyengine.core.port.outbound.message.MessageData;
import me.moirai.storyengine.core.port.outbound.message.MessageReader;

@Repository
public class MessageReaderImpl implements MessageReader {

    //@formatter:off
    private static final String GET_ALL_ACTIVE_BY_ADVENTURE = """
            SELECT m.public_id,
                   m.adventure_id,
                   m.created_by,
                   m.role,
                   m.content,
                   m.creation_date,
                   m.status
              FROM message m
              JOIN adventure a ON m.adventure_id = a.id
             WHERE a.public_id = :adventurePublicId
               AND m.status = 'ACTIVE'
             ORDER BY m.creation_date ASC
            """;
    //@formatter:on

    private final JdbcClient jdbcClient;

    public MessageReaderImpl(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public List<MessageData> getAllActiveByAdventureId(UUID adventurePublicId) {

        return jdbcClient.sql(GET_ALL_ACTIVE_BY_ADVENTURE)
                .param("adventurePublicId", adventurePublicId)
                .query(toMessageData())
                .list();
    }

    private RowMapper<MessageData> toMessageData() {
        return (rs, _) -> new MessageData(
                UUID.fromString(rs.getString("public_id")),
                rs.getLong("adventure_id"),
                rs.getString("created_by"),
                MessageAuthorRole.valueOf(rs.getString("role")),
                rs.getString("content"),
                rs.getTimestamp("creation_date").toInstant(),
                MessageStatus.valueOf(rs.getString("status")));
    }
}
