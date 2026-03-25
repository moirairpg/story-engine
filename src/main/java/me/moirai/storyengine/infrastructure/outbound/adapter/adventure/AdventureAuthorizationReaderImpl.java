package me.moirai.storyengine.infrastructure.outbound.adapter.adventure;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import me.moirai.storyengine.common.enums.Visibility;
import me.moirai.storyengine.core.port.inbound.AssetPermissionsData;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureAuthorizationReader;

@Repository
public class AdventureAuthorizationReaderImpl implements AdventureAuthorizationReader {

    //@formatter:off
    private static final String SELECT_PERMISSIONS = """
            SELECT MAX(mu.public_id) FILTER (WHERE ap.permission = 'OWNER') AS owner_id,
                   COALESCE(array_agg(mu.public_id) FILTER (WHERE ap.permission = 'WRITE'), ARRAY[]::UUID[]) AS writers,
                   COALESCE(array_agg(mu.public_id) FILTER (WHERE ap.permission = 'READ'), ARRAY[]::UUID[]) AS readers,
                   a.visibility
              FROM adventure a
              LEFT JOIN adventure_permissions ap ON ap.adventure_id = a.id
              LEFT JOIN moirai_user mu ON mu.id = ap.user_id
             WHERE a.public_id = :publicId
             GROUP BY a.visibility
            """;
    //@formatter:on

    private final JdbcClient jdbcClient;

    public AdventureAuthorizationReaderImpl(JdbcClient jdbcClient) {
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
