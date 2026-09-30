package me.moirai.storyengine.core.domain.userdetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import me.moirai.storyengine.common.exception.BusinessRuleViolationException;

public class UsernameTest {

    @Test
    public void shouldHoldTheHandleAsGivenWhenItIsValid() {

        // when
        var username = Username.of("john.doe_42");

        // then
        assertThat(username.getName()).isEqualTo("john.doe_42");
    }

    @Test
    public void shouldHoldTheHandleExactlyAsTypedWhenItHasMixedCase() {

        // when
        var username = Username.of("Merlin");

        // then
        assertThat(username.getName()).isEqualTo("Merlin");
    }

    @Test
    public void shouldAcceptTheHandleWhenItHasLeadingAndTrailingPeriods() {

        // when
        var username = Username.of(".a.b.");

        // then
        assertThat(username.getName()).isEqualTo(".a.b.");
    }

    @Test
    public void shouldThrowExceptionWhenTheHandleHasTwoConsecutivePeriods() {

        // then
        assertThatThrownBy(() -> Username.of("a..b"))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Username cannot contain two consecutive periods");
    }

    @Test
    public void shouldThrowExceptionWhenTheHandleIsShorterThanTwoCharacters() {

        // then
        assertThatThrownBy(() -> Username.of("a"))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Username must be between 2 and 32 characters");
    }

    @Test
    public void shouldThrowExceptionWhenTheHandleIsLongerThanThirtyTwoCharacters() {

        // then
        assertThatThrownBy(() -> Username.of("a".repeat(33)))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Username must be between 2 and 32 characters");
    }

    @Test
    public void shouldThrowExceptionWhenTheHandleHasASpace() {

        // then
        assertThatThrownBy(() -> Username.of("john doe"))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Username may only contain letters, digits, underscores and periods");
    }

    @Test
    public void shouldThrowExceptionWhenTheHandleHasAHyphen() {

        // then
        assertThatThrownBy(() -> Username.of("john-doe"))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Username may only contain letters, digits, underscores and periods");
    }

    @Test
    public void shouldThrowExceptionWhenTheHandleIsAReservedName() {

        // then
        assertThatThrownBy(() -> Username.of("Admin"))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("This name is reserved and cannot be used");
    }

    @Test
    public void shouldThrowExceptionWhenTheHandleIsBlank() {

        // then
        assertThatThrownBy(() -> Username.of("   "))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("Username is required");
    }

    @Test
    public void shouldBeEqualWithEqualHashCodesWhenHandlesDifferOnlyByCase() {

        // given
        var first = Username.of("Merlin");
        var second = Username.of("mERLIN");

        // then
        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }

    @Test
    public void shouldNotBeEqualWhenHandlesAreDifferent() {

        // given
        var first = Username.of("merlin");
        var second = Username.of("morgana");

        // then
        assertThat(first).isNotEqualTo(second);
    }
}
