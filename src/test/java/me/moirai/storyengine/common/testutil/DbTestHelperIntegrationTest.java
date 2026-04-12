package me.moirai.storyengine.common.testutil;

import static me.moirai.storyengine.common.enums.Role.ADMIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;

import me.moirai.storyengine.AbstractIntegrationTest;
import me.moirai.storyengine.core.domain.userdetails.User;
import me.moirai.storyengine.core.domain.userdetails.UserFixture;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldFixture;

public class DbTestHelperIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private JdbcClient jdbcClient;

    @BeforeEach
    public void setUp() {
        clearDatabase();
    }

    @Test
    public void shouldInsertPersistRowWithAllExpectedColumnValues() {

        // Given
        var user = UserFixture.player().discordId("unique-discord-1").build();

        // When
        insert(user, User.class);

        // Then
        var count = jdbcClient.sql("SELECT COUNT(*) FROM moirai_user WHERE discord_id = :discordId")
                .param("discordId", "unique-discord-1")
                .query(Long.class)
                .single();

        assertThat(count).isEqualTo(1L);

        var role = jdbcClient.sql("SELECT role FROM moirai_user WHERE discord_id = :discordId")
                .param("discordId", "unique-discord-1")
                .query(String.class)
                .single();

        assertThat(role).isEqualTo("PLAYER");
    }

    @Test
    public void shouldInsertExcludeGeneratedValueNullFields() {

        // Given
        var user = UserFixture.player().discordId("unique-discord-2").build();

        // When
        insert(user, User.class);

        // Then
        var exists = jdbcClient.sql("SELECT COUNT(*) FROM moirai_user WHERE discord_id = :discordId")
                .param("discordId", "unique-discord-2")
                .query(Long.class)
                .single();

        assertThat(exists).isEqualTo(1L);

        var numericId = jdbcClient.sql("SELECT id FROM moirai_user WHERE discord_id = :discordId")
                .param("discordId", "unique-discord-2")
                .query(Long.class)
                .single();

        assertThat(numericId).isNotNull().isPositive();
    }

    @Test
    public void shouldInsertGenerateUuidV7ForRandomUuidFieldThatWasNull() {

        // Given
        var user = UserFixture.player().discordId("unique-discord-3").build();

        // When
        insert(user, User.class);

        // Then
        var publicId = jdbcClient.sql("SELECT public_id FROM moirai_user WHERE discord_id = :discordId")
                .param("discordId", "unique-discord-3")
                .query(UUID.class)
                .single();

        assertThat(publicId).isNotNull();
    }

    @Test
    public void shouldInsertUseExistingValueOnRandomUuidFieldWhenAlreadySet() {

        // Given
        var existingUuid = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
        var user = UserFixture.player().discordId("unique-discord-4").build();
        org.springframework.test.util.ReflectionTestUtils.setField(user, "publicId", existingUuid);

        // When
        insert(user, User.class);

        // Then
        var publicId = jdbcClient.sql("SELECT public_id FROM moirai_user WHERE discord_id = :discordId")
                .param("discordId", "unique-discord-4")
                .query(UUID.class)
                .single();

        assertThat(publicId).isEqualTo(existingUuid);
    }

    @Test
    public void shouldInsertCorrectlyMapFieldsFromMappedSuperclassParent() {

        // Given
        var user = UserFixture.player()
                .discordId("unique-discord-5")
                .build();

        // When
        insert(user, User.class);

        // Then
        var createdBy = jdbcClient.sql("SELECT created_by FROM moirai_user WHERE discord_id = :discordId")
                .param("discordId", "unique-discord-5")
                .query(String.class)
                .single();

        assertThat(createdBy).isEqualTo("SYSTEM");
    }

    @Test
    public void shouldInsertCorrectlyMapFieldsFromEmbeddedValueObject() {

        // Given
        var world = WorldFixture.publicWorld()
                .narrator("TestNarrator", "Test personality")
                .build();

        // When
        insert(world, World.class);

        // Then
        var narratorName = jdbcClient.sql("SELECT narrator_name FROM world WHERE narrator_name = :name")
                .param("name", "TestNarrator")
                .query(String.class)
                .single();

        assertThat(narratorName).isNotNull().isNotBlank();
    }

    @Test
    public void shouldInsertThrowIllegalArgumentExceptionForNonEntityClass() {

        // Given
        var notAnEntity = "just a string";

        // When / Then
        assertThatThrownBy(() -> insert(notAnEntity, String.class))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldClearAndInsertRemoveExistingRowsBeforeInsertingNewOne() {

        // Given
        var first = UserFixture.player().discordId("unique-discord-6a").build();
        insert(first, User.class);

        var second = UserFixture.admin().discordId("unique-discord-6b").build();

        // When
        clearAndInsert(second, User.class);

        // Then
        var count = jdbcClient.sql("SELECT COUNT(*) FROM moirai_user")
                .query(Long.class)
                .single();

        assertThat(count).isEqualTo(1L);

        var role = jdbcClient.sql("SELECT role FROM moirai_user WHERE discord_id = :discordId")
                .param("discordId", "unique-discord-6b")
                .query(String.class)
                .single();

        assertThat(role).isEqualTo("ADMIN");
    }

    @Test
    public void shouldUpdateChangeCorrectColumnsOnMatchingRow() {

        // Given
        var user = UserFixture.player().discordId("unique-discord-7").build();
        insert(user, User.class);

        var insertedId = jdbcClient.sql("SELECT id FROM moirai_user WHERE discord_id = :discordId")
                .param("discordId", "unique-discord-7")
                .query(Long.class)
                .single();

        var insertedPublicId = jdbcClient.sql("SELECT public_id FROM moirai_user WHERE discord_id = :discordId")
                .param("discordId", "unique-discord-7")
                .query(UUID.class)
                .single();

        var updated = User.builder()
                .discordId("unique-discord-7")
                .username("john.doe")
                .role(ADMIN)
                .build();

        org.springframework.test.util.ReflectionTestUtils.setField(updated, "publicId", insertedPublicId);

        // When
        update(updated, insertedId, User.class);

        // Then
        var role = jdbcClient.sql("SELECT role FROM moirai_user WHERE id = :id")
                .param("id", insertedId)
                .query(String.class)
                .single();

        assertThat(role).isEqualTo("ADMIN");
    }

    @Test
    public void shouldUpdateNotAffectRowsOtherThanMatchingId() {

        // Given
        var first = UserFixture.player().discordId("unique-discord-8a").build();
        insert(first, User.class);

        var second = UserFixture.player().discordId("unique-discord-8b").build();
        insert(second, User.class);

        var firstId = jdbcClient.sql("SELECT id FROM moirai_user WHERE discord_id = :discordId")
                .param("discordId", "unique-discord-8a")
                .query(Long.class)
                .single();

        var firstPublicId = jdbcClient.sql("SELECT public_id FROM moirai_user WHERE discord_id = :discordId")
                .param("discordId", "unique-discord-8a")
                .query(UUID.class)
                .single();

        var updated = User.builder()
                .discordId("unique-discord-8a")
                .username("john.doe")
                .role(ADMIN)
                .build();

        org.springframework.test.util.ReflectionTestUtils.setField(updated, "publicId", firstPublicId);

        // When
        update(updated, firstId, User.class);

        // Then
        var secondRole = jdbcClient.sql("SELECT role FROM moirai_user WHERE discord_id = :discordId")
                .param("discordId", "unique-discord-8b")
                .query(String.class)
                .single();

        assertThat(secondRole).isEqualTo("PLAYER");
    }

    @Test
    public void shouldClearRemoveAllRowsFromTargetTable() {

        // Given
        insert(UserFixture.player().discordId("unique-discord-9a").build(), User.class);
        insert(UserFixture.admin().discordId("unique-discord-9b").build(), User.class);

        // When
        clear(User.class);

        // Then
        var count = jdbcClient.sql("SELECT COUNT(*) FROM moirai_user")
                .query(Long.class)
                .single();

        assertThat(count).isEqualTo(0L);
    }

    @Test
    public void shouldClearNotAffectOtherTables() {

        // Given
        insert(UserFixture.player().discordId("unique-discord-10").build(), User.class);
        insert(WorldFixture.publicWorld().build(), World.class);

        // When
        clear(User.class);

        // Then
        var worldCount = jdbcClient.sql("SELECT COUNT(*) FROM world")
                .query(Long.class)
                .single();

        assertThat(worldCount).isEqualTo(1L);
    }

    @Test
    public void shouldClearDatabaseRemoveAllRowsFromEveryDomainTable() {

        // Given
        insert(UserFixture.player().discordId("unique-discord-11").build(), User.class);
        insert(WorldFixture.publicWorld().build(), World.class);

        // When
        clearDatabase();

        // Then
        var userCount = jdbcClient.sql("SELECT COUNT(*) FROM moirai_user")
                .query(Long.class)
                .single();

        var worldCount = jdbcClient.sql("SELECT COUNT(*) FROM world")
                .query(Long.class)
                .single();

        assertThat(userCount).isEqualTo(0L);
        assertThat(worldCount).isEqualTo(0L);
    }

    @Test
    public void shouldClearDatabaseNotTruncateLiquibaseSystemTables() {

        // When
        clearDatabase();

        // Then
        var changelogCount = jdbcClient.sql("SELECT COUNT(*) FROM databasechangelog")
                .query(Long.class)
                .single();

        assertThat(changelogCount).isPositive();
    }
}
