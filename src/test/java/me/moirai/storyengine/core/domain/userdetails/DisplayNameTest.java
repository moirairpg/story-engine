package me.moirai.storyengine.core.domain.userdetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import me.moirai.storyengine.common.exception.BusinessRuleViolationException;

public class DisplayNameTest {

    @Test
    public void shouldAcceptTheNameWhenItHasSpacesAndAccents() {

        // when
        var displayName = DisplayName.of("Élise de la Tour");

        // then
        assertThat(displayName.getName()).isEqualTo("Élise de la Tour");
    }

    @Test
    public void shouldAcceptTheNameWhenItIsExactlyTwoCharacters() {

        // when
        var displayName = DisplayName.of("Jo");

        // then
        assertThat(displayName.getName()).isEqualTo("Jo");
    }

    @Test
    public void shouldThrowExceptionWhenTheNameIsShorterThanTwoCharacters() {

        // then
        assertThatThrownBy(() -> DisplayName.of("J"))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Display name must be between 2 and 32 characters");
    }

    @Test
    public void shouldThrowExceptionWhenTheNameIsLongerThanThirtyTwoCharacters() {

        // then
        assertThatThrownBy(() -> DisplayName.of("a".repeat(33)))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Display name must be between 2 and 32 characters");
    }

    @Test
    public void shouldThrowExceptionWhenTheNameIsAReservedNameInAnyCase() {

        // then
        assertThatThrownBy(() -> DisplayName.of("MoirAI"))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("This name is reserved and cannot be used");
    }

    @Test
    public void shouldThrowExceptionWhenTheNameIsBlank() {

        // then
        assertThatThrownBy(() -> DisplayName.of("   "))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Display name is required");
    }

    @Test
    public void shouldBeEqualWithEqualHashCodesWhenNamesAreIdentical() {

        // given
        var first = DisplayName.of("Merlin");
        var second = DisplayName.of("Merlin");

        // then
        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }

    @Test
    public void shouldNotBeEqualWhenNamesDifferOnlyByCase() {

        // given
        var first = DisplayName.of("Merlin");
        var second = DisplayName.of("merlin");

        // then
        assertThat(first).isNotEqualTo(second);
    }
}
