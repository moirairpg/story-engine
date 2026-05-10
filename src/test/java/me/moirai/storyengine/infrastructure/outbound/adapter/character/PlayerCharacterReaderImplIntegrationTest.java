package me.moirai.storyengine.infrastructure.outbound.adapter.character;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import me.moirai.storyengine.AbstractIntegrationTest;
import me.moirai.storyengine.core.domain.character.PlayerCharacter;
import me.moirai.storyengine.core.domain.character.PlayerCharacterFixture;
import me.moirai.storyengine.core.domain.userdetails.User;
import me.moirai.storyengine.core.domain.userdetails.UserFixture;
import me.moirai.storyengine.core.port.outbound.character.PlayerCharacterDetailsRow;
import me.moirai.storyengine.core.port.outbound.character.PlayerCharacterReader;

public class PlayerCharacterReaderImplIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private PlayerCharacterReader reader;

    @BeforeEach
    public void before() {
        clearDatabase();
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
}
