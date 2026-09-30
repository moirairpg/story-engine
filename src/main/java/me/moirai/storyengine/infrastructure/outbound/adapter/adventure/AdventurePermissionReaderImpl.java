package me.moirai.storyengine.infrastructure.outbound.adapter.adventure;

import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import me.moirai.storyengine.common.enums.PermissionLevel;
import me.moirai.storyengine.core.port.inbound.AssetMember;
import me.moirai.storyengine.core.port.outbound.adventure.AdventurePermissionReader;

@Repository
public class AdventurePermissionReaderImpl implements AdventurePermissionReader {

    //@formatter:off
    private static final String SELECT_MEMBERS_BY_ADVENTURE = """
            SELECT u.public_id    AS user_id,
                   u.username     AS username,
                   u.display_name AS display_name,
                   ap.permission  AS permission
              FROM adventure_permissions ap
                   JOIN adventure a   ON a.id = ap.adventure_id
                   JOIN moirai_user u ON u.id = ap.user_id
             WHERE a.public_id = :adventurePublicId
          ORDER BY CASE WHEN ap.permission = 'OWNER' THEN 0 ELSE 1 END,
                   u.display_name,
                   u.username
            """;
    //@formatter:on

    private final JdbcClient jdbcClient;

    public AdventurePermissionReaderImpl(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public List<AssetMember> getAllByAdventurePublicId(UUID adventurePublicId) {

        return jdbcClient.sql(SELECT_MEMBERS_BY_ADVENTURE)
                .param("adventurePublicId", adventurePublicId)
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
