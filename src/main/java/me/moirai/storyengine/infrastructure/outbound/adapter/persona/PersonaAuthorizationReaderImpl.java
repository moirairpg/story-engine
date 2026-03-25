package me.moirai.storyengine.infrastructure.outbound.adapter.persona;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import me.moirai.storyengine.common.enums.Visibility;
import me.moirai.storyengine.core.port.inbound.AssetPermissionsData;
import me.moirai.storyengine.core.port.outbound.persona.PersonaAuthorizationReader;

@Repository
public class PersonaAuthorizationReaderImpl implements PersonaAuthorizationReader {

    //@formatter:off
    private static final String SELECT_PERMISSIONS = """
            SELECT MAX(mu.public_id) FILTER (WHERE pp.permission = 'OWNER') AS owner_id,
                   COALESCE(array_agg(mu.public_id) FILTER (WHERE pp.permission = 'WRITE'), ARRAY[]::UUID[]) AS writers,
                   COALESCE(array_agg(mu.public_id) FILTER (WHERE pp.permission = 'READ'), ARRAY[]::UUID[]) AS readers,
                   p.visibility
              FROM persona p
              LEFT JOIN persona_permissions pp ON pp.persona_id = p.id
              LEFT JOIN moirai_user mu ON mu.id = pp.user_id
             WHERE p.public_id = :publicId
             GROUP BY p.visibility
            """;
    //@formatter:on

    private final JdbcClient jdbcClient;

    public PersonaAuthorizationReaderImpl(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Optional<AssetPermissionsData> getAuthorizationData(UUID publicId) {
        return jdbcClient.sql(SELECT_PERMISSIONS)
                .param("publicId", publicId)
                .query(toAssetPermissionsData())
                .optional();
    }

    private RowMapper<AssetPermissionsData> toAssetPermissionsData() {
        return (rs, _) -> {
            var ownerId = rs.getObject("owner_id", UUID.class);
            var writers = toList(rs.getArray("writers"));
            var readers = toList(rs.getArray("readers"));
            var visibility = Visibility.fromString(rs.getString("visibility"));
            return new AssetPermissionsData(ownerId, writers, readers, visibility);
        };
    }

    private List<UUID> toList(java.sql.Array array) throws java.sql.SQLException {
        return Arrays.asList((UUID[]) array.getArray());
    }
}
