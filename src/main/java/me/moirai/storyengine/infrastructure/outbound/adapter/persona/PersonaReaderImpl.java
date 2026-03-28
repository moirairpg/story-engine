package me.moirai.storyengine.infrastructure.outbound.adapter.persona;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import me.moirai.storyengine.common.dto.PermissionDto;
import me.moirai.storyengine.common.enums.PermissionLevel;
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
                   p.id AS numeric_id,
                   p.creation_date,
                   p.last_update_date
              FROM persona p
             WHERE p.public_id = :publicId
            """;

    private static final String SELECT_PERMISSIONS = """
            SELECT p.public_id,
                   pp.permission
              FROM persona_permissions pp
                   INNER JOIN persona p ON pp.persona_id = p.id
             WHERE pp.persona_id = :personaId
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
        return (rs, _) -> {
            var numericId = rs.getLong("numeric_id");
            var permissions = new HashSet<>(jdbcClient.sql(SELECT_PERMISSIONS)
                    .param("personaId", numericId)
                    .query((r, __) -> new PermissionDto(UUID.fromString(r.getString("public_id")),
                            PermissionLevel.valueOf(r.getString("permission"))))
                    .list());

            return new PersonaDetails(
                    UUID.fromString(rs.getString("public_id")),
                    rs.getString("name"),
                    rs.getString("personality"),
                    Visibility.valueOf(rs.getString("visibility")),
                    permissions,
                    rs.getTimestamp("creation_date").toInstant(),
                    rs.getTimestamp("last_update_date").toInstant());
        };
    }
}
