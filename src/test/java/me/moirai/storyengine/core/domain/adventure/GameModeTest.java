package me.moirai.storyengine.core.domain.adventure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import me.moirai.storyengine.common.exception.BusinessRuleViolationException;

public class GameModeTest {

    @Test
    public void gameMode_whenGetFromStringIsValid_thenReturnGameMode() {

        // Given
        String gameModeToSearch = "author";

        // When
        GameMode gameMode = GameMode.fromString(gameModeToSearch);

        // Then
        assertThat(gameMode).isEqualTo(GameMode.AUTHOR);
    }

    @Test
    public void gameMode_whenGetFromStringIsInvalid_thenThrowException() {

        // Given
        String gameModeToSearch = "invalid";

        // Then
        assertThrows(BusinessRuleViolationException.class, () -> GameMode.fromString(gameModeToSearch));
    }

    @Test
    public void gameMode_whenGetFromStringIsNull_thenThrowException() {

        // Given
        String gameModeToSearch = null;

        // Then
        assertThrows(BusinessRuleViolationException.class, () -> GameMode.fromString(gameModeToSearch));
    }
}
