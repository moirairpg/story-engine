package me.moirai.storyengine.infrastructure.outbound.adapter.adventure;

import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import me.moirai.storyengine.core.port.outbound.adventure.ChronicleSegmentData;
import me.moirai.storyengine.core.port.outbound.adventure.ChronicleSegmentReader;

@Repository
public class ChronicleSegmentReaderImpl implements ChronicleSegmentReader {

    //@formatter:off
    private static final String GET_ALL_BY_IDS = """
            SELECT cs.public_id,
                   cs.adventure_id,
                   cs.content,
                   cs.creation_date
              FROM chronicle_segment cs
             WHERE cs.public_id = ANY(:ids)
             ORDER BY cs.creation_date ASC
            """;

    private static final String GET_ALL_ORDERED = """
            SELECT cs.public_id,
                   cs.adventure_id,
                   cs.content,
                   cs.creation_date
              FROM chronicle_segment cs
              JOIN adventure a ON cs.adventure_id = a.id
             WHERE a.public_id = :adventurePublicId
             ORDER BY cs.creation_date ASC
            """;
    //@formatter:on

    private final JdbcClient jdbcClient;

    public ChronicleSegmentReaderImpl(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public List<ChronicleSegmentData> getAllByIds(List<UUID> publicIds) {

        if (publicIds.isEmpty()) {
            return List.of();
        }

        var ids = publicIds.toArray(UUID[]::new);

        return jdbcClient.sql(GET_ALL_BY_IDS)
                .param("ids", ids)
                .query(toChronicleSegmentData())
                .list();
    }

    @Override
    public List<ChronicleSegmentData> getAllOrdered(UUID adventurePublicId) {

        return jdbcClient.sql(GET_ALL_ORDERED)
                .param("adventurePublicId", adventurePublicId)
                .query(toChronicleSegmentData())
                .list();
    }

    private RowMapper<ChronicleSegmentData> toChronicleSegmentData() {
        return (rs, _) -> new ChronicleSegmentData(
                UUID.fromString(rs.getString("public_id")),
                rs.getLong("adventure_id"),
                rs.getString("content"),
                rs.getTimestamp("creation_date").toInstant());
    }
}
