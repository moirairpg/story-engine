package me.moirai.storyengine.core.domain.adventure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import me.moirai.storyengine.common.enums.Moderation;
import me.moirai.storyengine.common.exception.BusinessRuleViolationException;

public class ModerationTest {

    @Test
    public void convertFromString() {

        // Given
        String moderationString = "strict";

        // When
        Moderation moderation = Moderation.fromString(moderationString);

        // Then
        assertThat(moderation).isEqualTo(Moderation.STRICT);
        assertThat(moderation.isAbsolute()).isTrue();
        assertThat(moderation.getThresholds()).isNull();
    }

    @Test
    public void errorWhenModerationIsInvalid() {

        // Given
        String moderationString = "ahahahaha";

        // Then
        assertThrows(BusinessRuleViolationException.class, () -> Moderation.fromString(moderationString));
    }

    @Test
    public void errorWhenModerationIsNull() {

        // Given
        String moderationString = null;

        // Then
        assertThrows(BusinessRuleViolationException.class, () -> Moderation.fromString(moderationString));
    }
}
