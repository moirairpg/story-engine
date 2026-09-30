package me.moirai.storyengine.infrastructure.outbound.adapter.character;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import me.moirai.storyengine.common.enums.CharacterAttribute;
import me.moirai.storyengine.common.enums.CharacterClass;
import me.moirai.storyengine.common.enums.CharacterSkill;
import me.moirai.storyengine.common.enums.Visibility;
import me.moirai.storyengine.common.util.Functions;
import me.moirai.storyengine.core.port.inbound.AssetPermissionsData;
import me.moirai.storyengine.core.port.outbound.character.PlayerCharacterDetailsRow;
import me.moirai.storyengine.core.port.outbound.character.PlayerCharacterReader;
import me.moirai.storyengine.core.port.outbound.character.PlayerCharacterPermissionsData;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@Repository
public class PlayerCharacterReaderImpl implements PlayerCharacterReader {

    private static final String SELECT_BY_PUBLIC_ID = """
            SELECT  pc.public_id,
                    owner.username     AS owner_username,
                    owner.display_name AS owner_display_name,
                    pc.name,
                    pc.character_class,
                    pc.personality,
                    pc.physical_description,
                    pc.background,
                    pc.attributes,
                    pc.skills,
                    pc.xp,
                    pc.level,
                    pc.unspent_attribute_points,
                    pc.unspent_skill_points,
                    pc.image_key,
                    pc.ui_image_position_x,
                    pc.ui_image_position_y,
                    pc.creation_date,
                    pc.last_update_date
               FROM player_character pc
               JOIN moirai_user owner ON owner.id = pc.player_id
              WHERE pc.public_id = :characterId
            """;

    private static final String SELECT_OWNER_USERNAME = """
            SELECT owner.username AS owner_username
              FROM player_character pc
              JOIN moirai_user owner ON owner.id = pc.player_id
             WHERE pc.public_id = :characterId
            """;

    //@formatter:off
    private static final String SELECT_ENROLLED_ADVENTURE_PERMISSIONS = """
            SELECT (SELECT mu2.public_id
                      FROM adventure_permissions ap2
                           JOIN moirai_user mu2 ON mu2.id = ap2.user_id
                     WHERE ap2.adventure_id = a.id
                       AND ap2.permission = 'OWNER'
                     LIMIT 1) AS owner_id,
                   COALESCE(array_agg(mu.public_id) FILTER (WHERE ap.permission = 'WRITE'), ARRAY[]::UUID[]) AS writers,
                   COALESCE(array_agg(mu.public_id) FILTER (WHERE ap.permission = 'READ'), ARRAY[]::UUID[]) AS readers,
                   a.visibility
              FROM player_character pc
                   JOIN adventure_membership am       ON am.player_character_id = pc.id
                   JOIN adventure a                   ON a.id = am.adventure_id
                   LEFT JOIN adventure_permissions ap ON ap.adventure_id = a.id
                   LEFT JOIN moirai_user mu           ON mu.id = ap.user_id
             WHERE pc.public_id = :characterPublicId
             GROUP BY a.id, a.visibility
            """;
    //@formatter:on

    private final JdbcClient jdbcClient;
    private final JsonMapper jsonMapper;

    public PlayerCharacterReaderImpl(
            JdbcClient jdbcClient,
            JsonMapper jsonMapper) {

        this.jdbcClient = jdbcClient;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public Optional<PlayerCharacterDetailsRow> getById(UUID characterId) {

        return jdbcClient.sql(SELECT_BY_PUBLIC_ID)
                .param("characterId", characterId)
                .query(toPlayerCharacterDetailsRow())
                .optional();
    }

    @Override
    public Optional<String> getOwnerUsername(UUID characterId) {

        return jdbcClient.sql(SELECT_OWNER_USERNAME)
                .param("characterId", characterId)
                .query((rs, _) -> rs.getString("owner_username"))
                .optional();
    }

    @Override
    public Optional<PlayerCharacterPermissionsData> getPermissions(UUID characterId) {

        var ownerUsername = getOwnerUsername(characterId);

        if (ownerUsername.isEmpty()) {
            return Optional.empty();
        }

        var permissions = jdbcClient.sql(SELECT_ENROLLED_ADVENTURE_PERMISSIONS)
                .param("characterPublicId", characterId)
                .query(toAssetPermissionsData())
                .list();

        return Optional.of(new PlayerCharacterPermissionsData(ownerUsername.get(), permissions));
    }

    private RowMapper<AssetPermissionsData> toAssetPermissionsData() {

        return (rs, _) -> new AssetPermissionsData(
                rs.getObject("owner_id", UUID.class),
                Arrays.asList((UUID[]) rs.getArray("writers").getArray()),
                Arrays.asList((UUID[]) rs.getArray("readers").getArray()),
                Visibility.fromString(rs.getString("visibility")));
    }

    private static final String SIGNATURE_KEY = "signature";

    private RowMapper<PlayerCharacterDetailsRow> toPlayerCharacterDetailsRow() {

        return (rs, _) -> {

            var skillsByComponentName = toComponentLevels(rs.getString("skills"));

            return new PlayerCharacterDetailsRow(
                    rs.getObject("public_id", UUID.class),
                    rs.getString("owner_username"),
                    rs.getString("owner_display_name"),
                    rs.getString("name"),
                    Functions.mapOrNull(rs.getString("character_class"), CharacterClass::valueOf),
                    rs.getString("personality"),
                    rs.getString("physical_description"),
                    rs.getString("background"),
                    toAttributes(rs.getString("attributes")),
                    toSkills(skillsByComponentName),
                    Functions.mapOrNull(skillsByComponentName, levels -> levels.get(SIGNATURE_KEY)),
                    rs.getInt("xp"),
                    rs.getInt("level"),
                    rs.getInt("unspent_attribute_points"),
                    rs.getInt("unspent_skill_points"),
                    rs.getString("image_key"),
                    Functions.mapOrNull(rs.getBigDecimal("ui_image_position_x"), BigDecimal::doubleValue),
                    Functions.mapOrNull(rs.getBigDecimal("ui_image_position_y"), BigDecimal::doubleValue),
                    rs.getTimestamp("creation_date").toInstant(),
                    rs.getTimestamp("last_update_date").toInstant());
        };
    }

    private Map<String, Integer> toComponentLevels(String json) {

        return Functions.mapOrNull(json,
                s -> jsonMapper.readValue(s, new TypeReference<Map<String, Integer>>() {
                }));
    }

    private Map<CharacterAttribute, Integer> toAttributes(String json) {

        var levelsByComponentName = toComponentLevels(json);

        if (levelsByComponentName == null) {
            return null;
        }

        var attributes = new EnumMap<CharacterAttribute, Integer>(CharacterAttribute.class);
        levelsByComponentName.forEach((name, level) -> attributes
                .put(CharacterAttribute.valueOf(name.toUpperCase(Locale.ROOT)), level));

        return Collections.unmodifiableMap(attributes);
    }

    private Map<CharacterSkill, Integer> toSkills(Map<String, Integer> levelsByComponentName) {

        if (levelsByComponentName == null) {
            return null;
        }

        var skills = new EnumMap<CharacterSkill, Integer>(CharacterSkill.class);
        levelsByComponentName.forEach((name, level) -> {
            if (!SIGNATURE_KEY.equals(name)) {
                skills.put(CharacterSkill.valueOf(name.toUpperCase(Locale.ROOT)), level);
            }
        });

        return Collections.unmodifiableMap(skills);
    }
}
