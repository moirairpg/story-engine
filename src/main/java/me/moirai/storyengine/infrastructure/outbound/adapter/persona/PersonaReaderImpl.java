package me.moirai.storyengine.infrastructure.outbound.adapter.persona;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import me.moirai.storyengine.common.enums.Visibility;
import me.moirai.storyengine.core.port.inbound.persona.PersonaDetails;
import me.moirai.storyengine.core.port.outbound.persona.PersonaReader;

@Repository
public class PersonaReaderImpl implements PersonaReader {

    //@formatter:off
    private static final String SELECT_BY_ID = """
            SELECT p.public_id,
                   p.name,
                   p.personality,
                   p.visibility,
                   p.owner_id,
                   p.users_allowed_to_read,
                   p.users_allowed_to_write,
                   p.creation_date,
                   p.last_update_date
              FROM persona p
             WHERE p.public_id = :publicId
            """;
    //@formatter:on

    private final JdbcClient jdbcClient;

    public PersonaReaderImpl(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Optional<PersonaDetails> getPersonaById(UUID publicId) {
        return jdbcClient.sql(SELECT_BY_ID)
                .param("publicId", publicId)
                .query(toPersonaDetails())
                .optional();
    }

    private RowMapper<PersonaDetails> toPersonaDetails() {
        return (rs, _) -> new PersonaDetails(
                UUID.fromString(rs.getString("public_id")),
                rs.getString("name"),
                rs.getString("personality"),
                Visibility.valueOf(rs.getString("visibility")),
                rs.getString("owner_id"),
                parseStringSet(rs.getString("users_allowed_to_write")),
                parseStringSet(rs.getString("users_allowed_to_read")),
                rs.getTimestamp("creation_date").toInstant(),
                rs.getTimestamp("last_update_date").toInstant());
    }

    private Set<String> parseStringSet(String value) {
        if (value == null || value.isBlank()) return Set.of();
        return Arrays.stream(value.split(",")).map(String::trim).collect(Collectors.toSet());
    }
}
