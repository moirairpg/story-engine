package me.moirai.storyengine.core.domain.persona;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;

import me.moirai.storyengine.common.exception.BusinessRuleViolationException;
import me.moirai.storyengine.core.domain.adventure.GameMode;

public class GameModeTest {

    @Test
    public void gameMode_whenInputIsBlank_thenThrowBusinessRuleViolationException() {

        // Given
        String input = "";

        // Then
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> GameMode.fromString(input));
    }

    @Test
    public void gameMode_whenInputIsInvalid_thenThrowBusinessRuleViolationException() {

        // Given
        String input = "invalid";

        // Then
        assertThatExceptionOfType(BusinessRuleViolationException.class)
                .isThrownBy(() -> GameMode.fromString(input));
    }

    @Test
    public void gameMode_whenInputIsValid_thenGameModeIsReturned() {

        // Given
        String input = "rpg";

        // When
        GameMode gameMode = GameMode.fromString(input);

        // Then
        assertThat(gameMode).isEqualTo(GameMode.RPG);
    }
}
