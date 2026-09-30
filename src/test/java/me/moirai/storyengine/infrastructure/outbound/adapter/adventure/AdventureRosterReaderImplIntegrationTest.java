package me.moirai.storyengine.infrastructure.outbound.adapter.adventure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import me.moirai.storyengine.AbstractDatabaseIntegrationTest;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.character.PlayerCharacter;
import me.moirai.storyengine.core.domain.character.PlayerCharacterFixture;
import me.moirai.storyengine.core.domain.userdetails.User;
import me.moirai.storyengine.core.domain.userdetails.UserFixture;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRosterReader;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureMembershipSummaryRow;
import me.moirai.storyengine.core.port.outbound.adventure.CharacterAdventureSummaryRow;

public class AdventureRosterReaderImplIntegrationTest extends AbstractDatabaseIntegrationTest {

    @Autowired
    private AdventureRosterReader reader;

    private World world;

    @BeforeEach
    public void before() {

        clearDatabase();
        world = insert(WorldFixture.publicWorld().build(), World.class);
    }

    @Test
    public void shouldReturnAllAdventuresWhenCharacterIsEnrolledInSeveral() {

        // given
        var owner = insert(UserFixture.player().build(), User.class);
        var character = insertCharacter(owner);

        insertAdventure("Dragon Hunt", character);
        insertAdventure("The Sunken City", character);

        // when
        var result = reader.getAdventuresByPlayerCharacterPublicId(character.getPublicId());

        // then
        assertThat(result).hasSize(2)
                .extracting(CharacterAdventureSummaryRow::name)
                .containsExactlyInAnyOrder("Dragon Hunt", "The Sunken City");
    }

    @Test
    public void shouldReturnEmptyListWhenCharacterIsNotEnrolledAnywhere() {

        // given
        var owner = insert(UserFixture.player().build(), User.class);
        var character = insertCharacter(owner);

        insertAdventure("Dragon Hunt");

        // when
        var result = reader.getAdventuresByPlayerCharacterPublicId(character.getPublicId());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    public void shouldReturnEmptyListWhenCharacterDoesNotExist() {

        // when
        var result = reader.getAdventuresByPlayerCharacterPublicId(UUID.randomUUID());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    public void shouldReturnAllRegisteredCharactersWhenAdventureHasRoster() {

        // given
        var owner = insert(UserFixture.player().build(), User.class);
        var otherOwner = insertUser("55555", "player.two");
        var firstCharacter = insertCharacter(owner, "Volin Habar");
        var secondCharacter = insertCharacter(otherOwner, "Mira");

        var adventure = insertAdventure("Dragon Hunt", firstCharacter, secondCharacter);

        // when
        var result = reader.getAllByAdventurePublicId(adventure.getPublicId());

        // then
        assertThat(result).hasSize(2)
                .extracting(AdventureMembershipSummaryRow::name)
                .containsExactlyInAnyOrder("Volin Habar", "Mira");

        assertThat(result)
                .extracting(AdventureMembershipSummaryRow::playerUsername)
                .containsExactlyInAnyOrder(owner.getUsername(), otherOwner.getUsername());
    }

    @Test
    public void shouldCarryThePlayerDisplayNameWhenAdventureHasRoster() {

        // given
        var owner = insert(UserFixture.player().displayName("Johnny the Bold").build(), User.class);
        var character = insertCharacter(owner, "Volin Habar");
        var adventure = insertAdventure("Dragon Hunt", character);

        // when
        var result = reader.getAllByAdventurePublicId(adventure.getPublicId());

        // then
        assertThat(result).singleElement()
                .extracting(AdventureMembershipSummaryRow::playerUsername, AdventureMembershipSummaryRow::playerDisplayName)
                .containsExactly("john.doe", "Johnny the Bold");
    }

    @Test
    public void shouldReturnRawImageKeyWhenCharacterIsRegistered() {

        // given
        var owner = insert(UserFixture.player().build(), User.class);
        var character = insertCharacter(owner, "Volin Habar");
        var adventure = insertAdventure("Dragon Hunt", character);

        // when
        var result = reader.getAllByAdventurePublicId(adventure.getPublicId());

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().imageKey()).isEqualTo(character.getImageKey());
    }

    @Test
    public void shouldReturnEmptyListWhenAdventureHasNobodyRegistered() {

        // given
        var adventure = insertAdventure("Dragon Hunt");

        // when
        var result = reader.getAllByAdventurePublicId(adventure.getPublicId());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    public void shouldReturnTheCharacterImagePositionWhenAdventureHasRoster() {

        // given
        var owner = insert(UserFixture.player().build(), User.class);
        var character = PlayerCharacterFixture.samplePlayerCharacter()
                .name("Volin Habar")
                .playerId(owner.getId())
                .build();

        character.updateUiImagePosition(0.25, 0.75);
        insert(character, PlayerCharacter.class);

        var adventure = insertAdventureWithPosition("Dragon Hunt", 0.9, 0.1, character);

        // when
        var result = reader.getAllByAdventurePublicId(adventure.getPublicId());

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().uiImagePositionX()).isEqualTo(0.25);
        assertThat(result.getFirst().uiImagePositionY()).isEqualTo(0.75);
    }

    @Test
    public void shouldReturnTheAdventureImagePositionWhenListingAdventuresByCharacter() {

        // given
        var owner = insert(UserFixture.player().build(), User.class);
        var character = PlayerCharacterFixture.samplePlayerCharacter()
                .name("Volin Habar")
                .playerId(owner.getId())
                .build();

        character.updateUiImagePosition(0.25, 0.75);
        insert(character, PlayerCharacter.class);

        insertAdventureWithPosition("Dragon Hunt", 0.9, 0.1, character);

        // when
        var result = reader.getAdventuresByPlayerCharacterPublicId(character.getPublicId());

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().uiImagePositionX()).isEqualTo(0.9);
        assertThat(result.getFirst().uiImagePositionY()).isEqualTo(0.1);
    }

    @Test
    public void shouldReturnNoImagePositionWhenNoneIsSaved() {

        // given
        var owner = insert(UserFixture.player().build(), User.class);
        var character = insertCharacter(owner, "Volin Habar");
        var adventure = insertAdventure("Dragon Hunt", character);

        // when
        var roster = reader.getAllByAdventurePublicId(adventure.getPublicId());
        var adventures = reader.getAdventuresByPlayerCharacterPublicId(character.getPublicId());

        // then
        assertThat(roster.getFirst().uiImagePositionX()).isNull();
        assertThat(roster.getFirst().uiImagePositionY()).isNull();
        assertThat(adventures.getFirst().uiImagePositionX()).isNull();
        assertThat(adventures.getFirst().uiImagePositionY()).isNull();
    }

    private User insertUser(String discordId, String username) {

        return insert(UserFixture.player()
                .discordId(discordId)
                .username(username)
                .build(), User.class);
    }

    private PlayerCharacter insertCharacter(User owner) {

        return insert(PlayerCharacterFixture.samplePlayerCharacter()
                .playerId(owner.getId())
                .build(), PlayerCharacter.class);
    }

    private PlayerCharacter insertCharacter(User owner, String name) {

        return insert(PlayerCharacterFixture.samplePlayerCharacter()
                .name(name)
                .playerId(owner.getId())
                .build(), PlayerCharacter.class);
    }

    private Adventure insertAdventureWithPosition(String name, Double x, Double y, PlayerCharacter... characters) {

        var adventure = insert(AdventureFixture.publicAdventure()
                .name(name)
                .worldId(world.getPublicId())
                .build(), Adventure.class);

        adventure.updateUiImagePosition(x, y);

        for (var character : characters) {
            adventure.enrollPlayerCharacter(character.getId(), character.getPlayerId());
        }

        update(adventure, adventure.getId(), Adventure.class);

        return adventure;
    }

    private Adventure insertAdventure(String name, PlayerCharacter... characters) {

        var adventure = insert(AdventureFixture.publicAdventure()
                .name(name)
                .worldId(world.getPublicId())
                .build(), Adventure.class);

        if (characters.length == 0) {
            return adventure;
        }

        for (var character : characters) {
            adventure.enrollPlayerCharacter(character.getId(), character.getPlayerId());
        }

        update(adventure, adventure.getId(), Adventure.class);

        return adventure;
    }
}
