package me.moirai.storyengine.common.testutil;

import static me.moirai.storyengine.common.enums.Role.ADMIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;

import me.moirai.storyengine.AbstractIntegrationTest;
import me.moirai.storyengine.core.domain.persona.Persona;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.domain.userdetails.User;
import me.moirai.storyengine.core.domain.userdetails.UserFixture;
import me.moirai.storyengine.infrastructure.outbound.adapter.persona.PersonaJpaRepository;
import me.moirai.storyengine.infrastructure.outbound.adapter.userdetails.UserJpaRepository;

public class DbTestHelperTest extends AbstractIntegrationTest {

    @Autowired
    private JdbcClient jdbcClient;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private PersonaJpaRepository personaJpaRepository;

    @BeforeEach
    public void setUp() {
        personaJpaRepository.deleteAllInBatch();
        userJpaRepository.deleteAllInBatch();
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
        var now = OffsetDateTime.now();
        var user = UserFixture.player()
                .discordId("unique-discord-5")
                .creatorId("creator-abc")
                .creationDate(now)
                .lastUpdateDate(now)
                .build();

        // When
        insert(user, User.class);

        // Then
        var creatorId = jdbcClient.sql("SELECT creator_id FROM moirai_user WHERE discord_id = :discordId")
                .param("discordId", "unique-discord-5")
                .query(String.class)
                .single();

        assertThat(creatorId).isEqualTo("creator-abc");
    }

    @Test
    public void shouldInsertCorrectlyMapFieldsFromEmbeddedValueObject() {

        // Given
        var persona = PersonaFixture.publicPersona().name("TestPersona").build();

        // When
        insert(persona, Persona.class);

        // Then
        var ownerId = jdbcClient.sql("SELECT owner_id FROM persona WHERE name = :name")
                .param("name", "TestPersona")
                .query(String.class)
                .single();

        assertThat(ownerId).isNotNull().isNotBlank();
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
                .role(ADMIN)
                .creatorId("creator-xyz")
                .creationDate(OffsetDateTime.now())
                .lastUpdateDate(OffsetDateTime.now())
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
                .role(ADMIN)
                .creatorId("creator-updated")
                .creationDate(OffsetDateTime.now())
                .lastUpdateDate(OffsetDateTime.now())
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
        insert(PersonaFixture.publicPersona().name("KeepPersona").build(), Persona.class);

        // When
        clear(User.class);

        // Then
        var personaCount = jdbcClient.sql("SELECT COUNT(*) FROM persona")
                .query(Long.class)
                .single();

        assertThat(personaCount).isEqualTo(1L);
    }

    @Test
    public void shouldClearDatabaseRemoveAllRowsFromEveryDomainTable() {

        // Given
        insert(UserFixture.player().discordId("unique-discord-11").build(), User.class);
        insert(PersonaFixture.publicPersona().name("PersonaClearAll").build(), Persona.class);

        // When
        clearDatabase();

        // Then
        var userCount = jdbcClient.sql("SELECT COUNT(*) FROM moirai_user")
                .query(Long.class)
                .single();

        var personaCount = jdbcClient.sql("SELECT COUNT(*) FROM persona")
                .query(Long.class)
                .single();

        assertThat(userCount).isEqualTo(0L);
        assertThat(personaCount).isEqualTo(0L);
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
