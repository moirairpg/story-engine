package me.moirai.storyengine.core.domain;

import static me.moirai.storyengine.core.domain.CompletionRole.SYSTEM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import me.moirai.storyengine.common.exception.BusinessRuleViolationException;

public class CompletionRoleTest {

    @Test
    public void convertFromString() {

        // Given
        String completionRoleString = "system";

        // When
        CompletionRole completionRole = CompletionRole.fromString(completionRoleString);

        // Then
        assertThat(completionRole).isEqualTo(SYSTEM);
    }

    @Test
    public void errorWhenCompletionRoleIsInvalid() {

        // Given
        String completionRoleString = "ahahahaha";

        // Then
        assertThrows(BusinessRuleViolationException.class, () -> CompletionRole.fromString(completionRoleString));
    }

    @Test
    public void errorWhenCompletionRoleIsNull() {

        // Given
        String completionRoleString = null;

        // Then
        assertThrows(BusinessRuleViolationException.class, () -> CompletionRole.fromString(completionRoleString));
    }
}
