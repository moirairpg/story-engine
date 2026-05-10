package me.moirai.storyengine.infrastructure.outbound.adapter.character;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import me.moirai.storyengine.AbstractIntegrationTest;
import me.moirai.storyengine.common.enums.CharacterClass;
import me.moirai.storyengine.common.enums.PlayerCharacterSortField;
import me.moirai.storyengine.common.enums.SortDirection;
import me.moirai.storyengine.core.domain.character.PlayerCharacter;
import me.moirai.storyengine.core.domain.character.PlayerCharacterFixture;
import me.moirai.storyengine.core.domain.userdetails.User;
import me.moirai.storyengine.core.domain.userdetails.UserFixture;
import me.moirai.storyengine.core.port.inbound.character.SearchPlayerCharacters;
import me.moirai.storyengine.core.port.outbound.character.PlayerCharacterSearchReader;
import me.moirai.storyengine.core.port.outbound.character.PlayerCharacterSummaryRow;

public class PlayerCharacterSearchReaderImplIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private PlayerCharacterSearchReader reader;

    @BeforeEach
    public void before() {
        clearDatabase();
    }

    @Test
    void shouldReturnCharactersWhenUserOwnsThem() {

        // given
        var user = insert(UserFixture.player()
                .username("joao.das.couves")
                .discordId(String.valueOf(123L))
                .build(), User.class);

        insertSameNameCharacters(10, user);

        // when
        var result = reader.search(new SearchPlayerCharacters(
                null,
                null,
                null,
                null,
                1,
                10,
                user.getId()));

        // then
        assertThat(result).isNotNull();
        assertThat(result.data()).hasSize(10);
        assertThat(result.data().get(0).id()).isNotNull();
        assertThat(result.data().get(0).ownerUsername()).isEqualTo("joao.das.couves");
        assertThat(result.data().get(0).name()).isEqualTo("Conan the Barbarian");
        assertThat(result.data().get(0).characterClass()).isEqualTo(CharacterClass.BARBARIAN);
    }

    @Test
    void shouldReturnEmptyWhenUserOwnsNoCharacters() {

        // given
        var user = insert(UserFixture.player()
                .username("joao.das.couves")
                .discordId(String.valueOf(123L))
                .build(), User.class);

        insertSameNameCharacters(10, null);

        // when
        var result = reader.search(new SearchPlayerCharacters(
                null,
                null,
                null,
                null,
                1,
                10,
                user.getId()));

        // then
        assertThat(result).isNotNull();
        assertThat(result.data()).isEmpty();
    }

    @Test
    void shouldReturnOnlyOwnedCharactersWhenUserOwnsSome() {

        // given
        var user = insert(UserFixture.player()
                .username("joao.das.couves")
                .discordId(String.valueOf(123L))
                .build(), User.class);

        insertSameNameCharacters(5, user);
        insertSameNameCharacters(5, null);

        // when
        var result = reader.search(new SearchPlayerCharacters(
                null,
                null,
                null,
                null,
                1,
                10,
                user.getId()));

        // then
        assertThat(result).isNotNull();
        assertThat(result.data()).hasSize(5);
    }

    @Test
    void shouldFilterByNameSubstringIgnoringCaseWhenNameIsProvided() {

        // given
        var user = insert(UserFixture.player().build(), User.class);
        insertNamed("Volin Habar", CharacterClass.PALADIN, null, user);
        insertNamed("Conan the Barbarian", CharacterClass.BARBARIAN, null, user);
        insertNamed("Volgar the Mage", CharacterClass.MAGE, null, user);

        // when
        var result = reader.search(new SearchPlayerCharacters(
                "vol",
                null,
                null,
                null,
                1,
                10,
                user.getId()));

        // then
        assertThat(result.data()).extracting(PlayerCharacterSummaryRow::name)
                .containsExactlyInAnyOrder("Volin Habar", "Volgar the Mage");
    }

    @Test
    void shouldFilterByCharacterClassWhenClassIsProvided() {

        // given
        var user = insert(UserFixture.player().build(), User.class);
        insertNamed("Alpha", CharacterClass.BARD, null, user);
        insertNamed("Bravo", CharacterClass.MAGE, null, user);
        insertNamed("Charlie", CharacterClass.MAGE, null, user);

        // when
        var result = reader.search(new SearchPlayerCharacters(
                null,
                CharacterClass.MAGE,
                null,
                null,
                1,
                10,
                user.getId()));

        // then
        assertThat(result.data()).extracting(PlayerCharacterSummaryRow::name)
                .containsExactlyInAnyOrder("Bravo", "Charlie");
    }

    @Test
    void shouldSortByNameAscendingWhenSortFieldIsNameAndDirectionIsAsc() {

        // given
        var user = insert(UserFixture.player().build(), User.class);
        insertSortFixture(user);

        // when
        var result = reader.search(new SearchPlayerCharacters(
                null,
                null,
                PlayerCharacterSortField.NAME,
                SortDirection.ASC,
                1,
                10,
                user.getId()));

        // then
        assertThat(result.data()).extracting(PlayerCharacterSummaryRow::name)
                .containsExactly("Alpha", "Bravo", "Charlie");
    }

    @Test
    void shouldSortByNameDescendingWhenSortFieldIsNameAndDirectionIsDesc() {

        // given
        var user = insert(UserFixture.player().build(), User.class);
        insertSortFixture(user);

        // when
        var result = reader.search(new SearchPlayerCharacters(
                null,
                null,
                PlayerCharacterSortField.NAME,
                SortDirection.DESC,
                1,
                10,
                user.getId()));

        // then
        assertThat(result.data()).extracting(PlayerCharacterSummaryRow::name)
                .containsExactly("Charlie", "Bravo", "Alpha");
    }

    @Test
    void shouldSortByCreationDateAscendingWhenSortFieldIsCreationDateAndDirectionIsAsc() {

        // given
        var user = insert(UserFixture.player().build(), User.class);
        insertSortFixture(user);

        // when
        var result = reader.search(new SearchPlayerCharacters(
                null,
                null,
                PlayerCharacterSortField.CREATION_DATE,
                SortDirection.ASC,
                1,
                10,
                user.getId()));

        // then
        assertThat(result.data()).extracting(PlayerCharacterSummaryRow::name)
                .containsExactly("Bravo", "Charlie", "Alpha");
    }

    @Test
    void shouldSortByCreationDateDescendingWhenSortFieldIsCreationDateAndDirectionIsDesc() {

        // given
        var user = insert(UserFixture.player().build(), User.class);
        insertSortFixture(user);

        // when
        var result = reader.search(new SearchPlayerCharacters(
                null,
                null,
                PlayerCharacterSortField.CREATION_DATE,
                SortDirection.DESC,
                1,
                10,
                user.getId()));

        // then
        assertThat(result.data()).extracting(PlayerCharacterSummaryRow::name)
                .containsExactly("Alpha", "Charlie", "Bravo");
    }

    @Test
    void shouldSortByLastUpdateDateAscendingWhenSortFieldIsLastUpdateDateAndDirectionIsAsc() {

        // given
        var user = insert(UserFixture.player().build(), User.class);
        insertSortFixture(user);

        // when
        var result = reader.search(new SearchPlayerCharacters(
                null,
                null,
                PlayerCharacterSortField.LAST_UPDATE_DATE,
                SortDirection.ASC,
                1,
                10,
                user.getId()));

        // then
        assertThat(result.data()).extracting(PlayerCharacterSummaryRow::name)
                .containsExactly("Alpha", "Bravo", "Charlie");
    }

    @Test
    void shouldSortByLastUpdateDateDescendingWhenSortFieldIsLastUpdateDateAndDirectionIsDesc() {

        // given
        var user = insert(UserFixture.player().build(), User.class);
        insertSortFixture(user);

        // when
        var result = reader.search(new SearchPlayerCharacters(
                null,
                null,
                PlayerCharacterSortField.LAST_UPDATE_DATE,
                SortDirection.DESC,
                1,
                10,
                user.getId()));

        // then
        assertThat(result.data()).extracting(PlayerCharacterSummaryRow::name)
                .containsExactly("Charlie", "Bravo", "Alpha");
    }

    @Test
    void shouldRespectPageAndSizeWhenPaginationIsProvided() {

        // given
        var user = insert(UserFixture.player().build(), User.class);
        insertSameNameCharacters(5, user);

        // when
        var firstPage = reader.search(new SearchPlayerCharacters(
                null, null, null, null, 1, 2, user.getId()));
        var secondPage = reader.search(new SearchPlayerCharacters(
                null, null, null, null, 2, 2, user.getId()));
        var thirdPage = reader.search(new SearchPlayerCharacters(
                null, null, null, null, 3, 2, user.getId()));

        // then
        assertThat(firstPage.data()).hasSize(2);
        assertThat(secondPage.data()).hasSize(2);
        assertThat(thirdPage.data()).hasSize(1);

        assertThat(firstPage.totalItems()).isEqualTo(5);
        assertThat(secondPage.totalItems()).isEqualTo(5);
        assertThat(thirdPage.totalItems()).isEqualTo(5);
    }

    @Test
    void shouldApplyDefaultsWhenSortAndPaginationAreNull() {

        // given
        var user = insert(UserFixture.player().build(), User.class);
        insertSortFixture(user);

        // when
        var result = reader.search(new SearchPlayerCharacters(
                null,
                null,
                null,
                null,
                null,
                null,
                user.getId()));

        // then
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.data()).hasSize(3);
        assertThat(result.data()).extracting(PlayerCharacterSummaryRow::name)
                .containsExactly("Alpha", "Charlie", "Bravo");
    }

    private void insertSameNameCharacters(int amountOfResults, User owner) {

        var resolvedOwner = owner != null ? owner : insert(UserFixture.player().build(), User.class);
        for (int i = 0; i < amountOfResults; i++) {
            var character = PlayerCharacterFixture.samplePlayerCharacter()
                    .name("Conan the Barbarian")
                    .characterClass(CharacterClass.BARBARIAN)
                    .personality("Brave and strong")
                    .physicalDescription("Tall and muscular")
                    .playerId(resolvedOwner.getId())
                    .build();

            character.updateImageKey("conan-image-key");
            insert(character, PlayerCharacter.class);
        }
    }

    private void insertNamed(String name, CharacterClass characterClass, Instant creationDate, User owner) {

        var character = PlayerCharacterFixture.samplePlayerCharacter()
                .name(name)
                .characterClass(characterClass)
                .playerId(owner.getId())
                .build();

        if (creationDate != null) {
            character.setCreationDate(creationDate);
        }

        insert(character, PlayerCharacter.class);
    }

    private void insertSortFixture(User owner) {

        var now = Instant.now();
        insertNamed("Alpha", CharacterClass.BARD, now.minus(1, ChronoUnit.DAYS), owner);
        sleepShortly();
        insertNamed("Bravo", CharacterClass.CLERIC, now.minus(3, ChronoUnit.DAYS), owner);
        sleepShortly();
        insertNamed("Charlie", CharacterClass.MAGE, now.minus(2, ChronoUnit.DAYS), owner);
    }

    private void sleepShortly() {

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
