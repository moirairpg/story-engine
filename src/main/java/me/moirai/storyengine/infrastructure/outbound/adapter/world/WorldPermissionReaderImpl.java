package me.moirai.storyengine.infrastructure.outbound.adapter.world;

import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import me.moirai.storyengine.common.enums.PermissionLevel;
import me.moirai.storyengine.core.port.inbound.AssetMember;
import me.moirai.storyengine.core.port.outbound.world.WorldPermissionReader;

@Repository
public class WorldPermissionReaderImpl implements WorldPermissionReader {

    //@formatter:off
    private static final String SELECT_MEMBERS_BY_WORLD = """
            SELECT u.public_id    AS user_id,
                   u.username     AS username,
                   u.display_name AS display_name,
                   wp.permission  AS permission
              FROM world_permissions wp
                   JOIN world w       ON w.id = wp.world_id
                   JOIN moirai_user u ON u.id = wp.user_id
             WHERE w.public_id = :worldPublicId
          ORDER BY CASE WHEN wp.permission = 'OWNER' THEN 0 ELSE 1 END,
                   u.display_name,
                   u.username
            """;
    //@formatter:on

    private final JdbcClient jdbcClient;

    public WorldPermissionReaderImpl(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public List<AssetMember> getAllByWorldPublicId(UUID worldPublicId) {

        return jdbcClient.sql(SELECT_MEMBERS_BY_WORLD)
                .param("worldPublicId", worldPublicId)
                .query(toAssetMember())
                .list();
    }

    private RowMapper<AssetMember> toAssetMember() {

        return (rs, _) -> new AssetMember(
                rs.getObject("user_id", UUID.class),
                rs.getString("username"),
                rs.getString("display_name"),
                PermissionLevel.valueOf(rs.getString("permission")));
    }
}
