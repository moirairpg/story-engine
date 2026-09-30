package me.moirai.storyengine.infrastructure.outbound.adapter.character;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import me.moirai.storyengine.AbstractDatabaseIntegrationTest;
import me.moirai.storyengine.common.domain.Permission;
import me.moirai.storyengine.common.enums.CharacterSkill;
import me.moirai.storyengine.common.enums.PermissionLevel;
import me.moirai.storyengine.common.enums.Visibility;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.character.PlayerCharacter;
import me.moirai.storyengine.core.domain.character.PlayerCharacterFixture;
import me.moirai.storyengine.core.domain.character.SkillLevels;
import me.moirai.storyengine.core.domain.userdetails.User;
import me.moirai.storyengine.core.domain.userdetails.UserFixture;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.port.inbound.AssetPermissionsData;
import me.moirai.storyengine.core.port.outbound.character.PlayerCharacterDetailsRow;
import me.moirai.storyengine.core.port.outbound.character.PlayerCharacterReader;

public class PlayerCharacterReaderImplIntegrationTest extends AbstractDatabaseIntegrationTest {

    @Autowired
    private PlayerCharacterReader reader;

    private World world;

    @BeforeEach
    public void before() {

        clearDatabase();
        world = insert(WorldFixture.publicWorld().build(), World.class);
    }

    @Test
    void shouldReturnCharacterDetailsWhenCharacterExists() {

        // given
        var owner = insert(UserFixture.player().build(), User.class);
        var character = PlayerCharacterFixture.samplePlayerCharacter()
                .playerId(owner.getId())
                .build();

        insert(character, PlayerCharacter.class);

        // when
        var result = reader.getById(character.getPublicId());

        // then
        assertThat(result).isPresent()
                .get()
                .extracting(PlayerCharacterDetailsRow::id).isEqualTo(character.getPublicId());
    }

    @Test
    void shouldReturnTheOwnerDisplayNameWhenGettingTheCharacterById() {

        // given
        var owner = insert(UserFixture.player().displayName("Johnny the Bold").build(), User.class);
        var character = PlayerCharacterFixture.samplePlayerCharacter()
                .playerId(owner.getId())
                .build();

        insert(character, PlayerCharacter.class);

        // when
        var result = reader.getById(character.getPublicId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().ownerUsername()).isEqualTo("john.doe");
        assertThat(result.get().ownerDisplayName()).isEqualTo("Johnny the Bold");
    }

    @Test
    void shouldReturnTheBackgroundWhenGettingTheCharacterById() {

        // given
        var owner = insert(UserFixture.player().build(), User.class);
        var character = PlayerCharacterFixture.samplePlayerCharacter()
                .playerId(owner.getId())
                .build();

        insert(character, PlayerCharacter.class);

        // when
        var result = reader.getById(character.getPublicId());

        // then
        assertThat(result).isPresent()
                .get()
                .extracting(PlayerCharacterDetailsRow::background)
                .isEqualTo("Raised in a cliffside monastery, he took the oath after his village burned.");
    }

    @Test
    void shouldReturnTheAttributeLevelsWhenGettingTheCharacterById() {

        // given
        var owner = insert(UserFixture.player().build(), User.class);
        var character = PlayerCharacterFixture.samplePlayerCharacter()
                .playerId(owner.getId())
                .build();

        insert(character, PlayerCharacter.class);

        // when
        var result = reader.getById(character.getPublicId());

        // then
        assertThat(result).isPresent()
                .get()
                .extracting(PlayerCharacterDetailsRow::attributes)
                .isEqualTo(PlayerCharacterFixture.sampleAttributeAllocation());
    }

    @Test
    void shouldReturnTheSkillLevelsAndSignatureLevelWhenGettingTheCharacterById() {

        // given
        var owner = insert(UserFixture.player().build(), User.class);
        var character = PlayerCharacterFixture.samplePlayerCharacter()
                .playerId(owner.getId())
                .build();

        insert(character, PlayerCharacter.class);

        // when
        var result = reader.getById(character.getPublicId());

        // then
        assertThat(result).isPresent().get().satisfies(row -> {
            assertThat(row.skills()).isEqualTo(PlayerCharacterFixture.sampleSkillAllocation());
            assertThat(row.signatureLevel()).isEqualTo(1);
        });
    }

    @Test
    void shouldReturnTheEmptySheetWhenTheCharacterHasNoClass() {

        // given
        var owner = insert(UserFixture.player().build(), User.class);
        var character = PlayerCharacterFixture.samplePlayerCharacter()
                .playerId(owner.getId())
                .build();

        var untrained = new EnumMap<CharacterSkill, Integer>(CharacterSkill.class);
        Arrays.stream(CharacterSkill.values()).forEach(skill -> untrained.put(skill, 0));

        ReflectionTestUtils.setField(character, "characterClass", null);
        ReflectionTestUtils.setField(character, "skillLevels", SkillLevels.of(untrained, 0));

        insert(character, PlayerCharacter.class);

        // when
        var result = reader.getById(character.getPublicId());

        // then
        assertThat(result).isPresent().get().satisfies(row -> {
            assertThat(row.characterClass()).isNull();
            assertThat(row.skills()).isEqualTo(untrained);
            assertThat(row.signatureLevel()).isZero();
        });
    }

    @Test
    void shouldReturnImagePositionWhenCharacterExists() {

        // given
        var owner = insert(UserFixture.player().build(), User.class);
        var character = PlayerCharacterFixture.samplePlayerCharacter()
                .playerId(owner.getId())
                .build();

        character.updateUiImagePosition(0.25, 0.75);

        insert(character, PlayerCharacter.class);

        // when
        var result = reader.getById(character.getPublicId());

        // then
        assertThat(result).isPresent().get().satisfies(row -> {
            assertThat(row.uiImagePositionX()).isEqualTo(0.25);
            assertThat(row.uiImagePositionY()).isEqualTo(0.75);
        });
    }

    @Test
    void shouldReturnNullImagePositionWhenCharacterHasNone() {

        // given
        var owner = insert(UserFixture.player().build(), User.class);
        var character = PlayerCharacterFixture.samplePlayerCharacter()
                .playerId(owner.getId())
                .build();

        insert(character, PlayerCharacter.class);

        // when
        var result = reader.getById(character.getPublicId());

        // then
        assertThat(result).isPresent().get().satisfies(row -> {
            assertThat(row.uiImagePositionX()).isNull();
            assertThat(row.uiImagePositionY()).isNull();
        });
    }

    @Test
    void shouldReturnEmptyWhenCharacterDoesNotExist() {

        // when
        var result = reader.getById(UUID.randomUUID());

        // then
        assertThat(result).isNotPresent();
    }

    @Test
    void shouldReturnEmptyWhenOwnerDoesNotExist() {

        // given
        var character = PlayerCharacterFixture.samplePlayerCharacter().build();
        insert(character, PlayerCharacter.class);

        // when
        var result = reader.getById(character.getPublicId());

        // then
        assertThat(result).isNotPresent();
    }

    @Test
    void shouldReturnOwnerUsernameWhenCharacterExists() {

        // given
        var owner = insert(UserFixture.player().build(), User.class);
        var character = PlayerCharacterFixture.samplePlayerCharacter()
                .playerId(owner.getId())
                .build();

        insert(character, PlayerCharacter.class);

        // when
        var result = reader.getOwnerUsername(character.getPublicId());

        // then
        assertThat(result).isPresent()
                .get()
                .isEqualTo(owner.getUsername());
    }

    @Test
    void shouldReturnEmptyOwnerUsernameWhenCharacterDoesNotExist() {

        // when
        var result = reader.getOwnerUsername(UUID.randomUUID());

        // then
        assertThat(result).isNotPresent();
    }

    @Test
    void shouldReturnEmptyOwnerUsernameWhenOwnerDoesNotExist() {

        // given
        var character = PlayerCharacterFixture.samplePlayerCharacter().build();
        insert(character, PlayerCharacter.class);

        // when
        var result = reader.getOwnerUsername(character.getPublicId());

        // then
        assertThat(result).isNotPresent();
    }

    @Test
    void shouldReturnEmptyVisibilityDataWhenCharacterDoesNotExist() {

        // when
        var result = reader.getPermissions(UUID.randomUUID());

        // then
        assertThat(result).isNotPresent();
    }

    @Test
    void shouldReturnOwnerWithoutPermissionsWhenCharacterIsRegisteredNowhere() {

        // given
        var owner = insert(UserFixture.player().build(), User.class);
        var character = insertCharacter(owner);

        // when
        var result = reader.getPermissions(character.getPublicId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().ownerUsername()).isEqualTo(owner.getUsername());
        assertThat(result.get().enrolledAdventurePermissions()).isEmpty();
    }

    @Test
    void shouldReturnAdventurePermissionsWhenRegisteredAdventureIsPublic() {

        // given
        var owner = insert(UserFixture.player().build(), User.class);
        var writer = insertUser("writer", "22222");
        var readerUser = insertUser("reader", "33333");
        var character = insertCharacter(owner);

        insertAdventure(Visibility.PUBLIC, owner, writer, readerUser, character);

        // when
        var result = reader.getPermissions(character.getPublicId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().enrolledAdventurePermissions()).hasSize(1);

        var permissions = result.get().enrolledAdventurePermissions().getFirst();

        assertThat(permissions.visibility()).isEqualTo(Visibility.PUBLIC);
        assertThat(permissions.ownerId()).isEqualTo(owner.getPublicId());
        assertThat(permissions.writers()).containsExactly(writer.getPublicId());
        assertThat(permissions.readers()).containsExactly(readerUser.getPublicId());
    }

    @Test
    void shouldReturnAdventurePermissionsWhenRegisteredAdventureIsPrivate() {

        // given
        var owner = insert(UserFixture.player().build(), User.class);
        var writer = insertUser("writer", "22222");
        var readerUser = insertUser("reader", "33333");
        var character = insertCharacter(owner);

        insertAdventure(Visibility.PRIVATE, owner, writer, readerUser, character);

        // when
        var result = reader.getPermissions(character.getPublicId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().enrolledAdventurePermissions()).hasSize(1);

        var permissions = result.get().enrolledAdventurePermissions().getFirst();

        assertThat(permissions.visibility()).isEqualTo(Visibility.PRIVATE);
        assertThat(permissions.ownerId()).isEqualTo(owner.getPublicId());
        assertThat(permissions.writers()).containsExactly(writer.getPublicId());
        assertThat(permissions.readers()).containsExactly(readerUser.getPublicId());
    }

    @Test
    void shouldReturnSeparateEntriesWhenCharacterIsRegisteredInSeveralAdventures() {

        // given
        var owner = insert(UserFixture.player().build(), User.class);
        var writer = insertUser("writer", "22222");
        var readerUser = insertUser("reader", "33333");
        var character = insertCharacter(owner);

        insertAdventure(Visibility.PUBLIC, owner, writer, null, character);
        insertAdventure(Visibility.PRIVATE, owner, null, readerUser, character);

        // when
        var result = reader.getPermissions(character.getPublicId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().enrolledAdventurePermissions()).hasSize(2);

        var publicAdventure = permissionsWith(result.get().enrolledAdventurePermissions(), Visibility.PUBLIC);
        var privateAdventure = permissionsWith(result.get().enrolledAdventurePermissions(), Visibility.PRIVATE);

        assertThat(publicAdventure.writers()).containsExactly(writer.getPublicId());
        assertThat(publicAdventure.readers()).isEmpty();
        assertThat(privateAdventure.writers()).isEmpty();
        assertThat(privateAdventure.readers()).containsExactly(readerUser.getPublicId());
    }

    @Test
    void shouldReturnEmptyWriterAndReaderListsWhenAdventureOnlyHasAnOwner() {

        // given
        var owner = insert(UserFixture.player().build(), User.class);
        var character = insertCharacter(owner);

        insertAdventure(Visibility.PRIVATE, owner, null, null, character);

        // when
        var result = reader.getPermissions(character.getPublicId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().enrolledAdventurePermissions()).hasSize(1);

        var permissions = result.get().enrolledAdventurePermissions().getFirst();

        assertThat(permissions.ownerId()).isEqualTo(owner.getPublicId());
        assertThat(permissions.writers()).isNotNull().isEmpty();
        assertThat(permissions.readers()).isNotNull().isEmpty();
    }

    private AssetPermissionsData permissionsWith(List<AssetPermissionsData> permissions, Visibility visibility) {

        return permissions.stream()
                .filter(entry -> entry.visibility() == visibility)
                .findFirst()
                .orElseThrow();
    }

    private User insertUser(String username, String discordId) {

        return insert(UserFixture.player()
                .username(username)
                .discordId(discordId)
                .build(), User.class);
    }

    private PlayerCharacter insertCharacter(User owner) {

        return insert(PlayerCharacterFixture.samplePlayerCharacter()
                .playerId(owner.getId())
                .build(), PlayerCharacter.class);
    }

    private Adventure insertAdventure(
            Visibility visibility,
            User owner,
            User writer,
            User reader,
            PlayerCharacter character) {

        var permissions = new ArrayList<Permission>();
        permissions.add(new Permission(owner.getId(), PermissionLevel.OWNER));

        if (writer != null) {
            permissions.add(new Permission(writer.getId(), PermissionLevel.WRITE));
        }

        if (reader != null) {
            permissions.add(new Permission(reader.getId(), PermissionLevel.READ));
        }

        var adventure = insert(AdventureFixture.publicAdventure()
                .worldId(world.getPublicId())
                .visibility(visibility)
                .permissions(permissions.toArray(Permission[]::new))
                .build(), Adventure.class);

        adventure.enrollPlayerCharacter(character.getId(), character.getPlayerId());

        update(adventure, adventure.getId(), Adventure.class);

        return adventure;
    }
}
