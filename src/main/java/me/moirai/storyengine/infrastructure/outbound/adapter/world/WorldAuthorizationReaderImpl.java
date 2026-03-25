package me.moirai.storyengine.infrastructure.outbound.adapter.world;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import me.moirai.storyengine.common.enums.Visibility;
import me.moirai.storyengine.core.port.inbound.AssetPermissionsData;
import me.moirai.storyengine.core.port.outbound.world.WorldAuthorizationReader;

@Repository
public class WorldAuthorizationReaderImpl implements WorldAuthorizationReader {

    //@formatter:off
    private static final String SELECT_PERMISSIONS = """
            SELECT MAX(mu.public_id) FILTER (WHERE wp.permission = 'OWNER') AS owner_id,
                   COALESCE(array_agg(mu.public_id) FILTER (WHERE wp.permission = 'WRITE'), ARRAY[]::UUID[]) AS writers,
                   COALESCE(array_agg(mu.public_id) FILTER (WHERE wp.permission = 'READ'), ARRAY[]::UUID[]) AS readers,
                   w.visibility
              FROM world w
              LEFT JOIN world_permissions wp ON wp.world_id = w.id
              LEFT JOIN moirai_user mu ON mu.id = wp.user_id
             WHERE w.public_id = :publicId
             GROUP BY w.visibility
            """;
    //@formatter:on

    private final JdbcClient jdbcClient;

    public WorldAuthorizationReaderImpl(JdbcClient jdbcClient) {
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
