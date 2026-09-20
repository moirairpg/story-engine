package me.moirai.storyengine.core.domain.userdetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import me.moirai.storyengine.common.enums.Role;
import me.moirai.storyengine.common.exception.BusinessRuleViolationException;

public class UserTest {

    private static final Long NUMERIC_ID = 42L;
    private static final UUID OTHER_USER_ID = UUID.fromString("00000000-0000-0000-0000-0000000000aa");

    @Test
    public void shouldEmitUserDeletedEventWhenDeletionIsCommunicated() {

        // given
        var user = userWithId();

        // when
        user.communicateUserDeleted();

        // then
        assertThat(user.drainEvents())
                .singleElement()
                .isInstanceOf(UserDeletedEvent.class);
    }

    @Test
    public void shouldCarryTheUserIdentityOnTheDeletionEvent() {

        // given
        var user = userWithId();

        // when
        user.communicateUserDeleted();

        // then
        var event = (UserDeletedEvent) user.drainEvents().getFirst();

        assertThat(event.getUserId()).isEqualTo(NUMERIC_ID);
        assertThat(event.getUserPublicId()).isEqualTo(user.getPublicId());
        assertThat(event.getUsername()).isEqualTo(user.getUsername());
    }

    @Test
    public void shouldReturnNoEventsWhenNothingHasBeenCommunicated() {

        // given
        var user = userWithId();

        // then
        assertThat(user.drainEvents()).isEmpty();
    }

    @Test
    public void shouldClearEventsOnceDrainedWhenDrainedTwice() {

        // given
        var user = userWithId();
        user.communicateUserDeleted();

        // when
        user.drainEvents();

        // then
        assertThat(user.drainEvents()).isEmpty();
    }

    @Test
    public void shouldBeActiveWhenTheAccountIsCreated() {

        // given
        var user = userWithId();

        // then
        assertThat(user.isActive()).isTrue();
    }

    @Test
    public void shouldBeInactiveWhenDeactivatedBySomeoneElse() {

        // given
        var user = userWithId();

        // when
        user.updateActiveState(false, OTHER_USER_ID);

        // then
        assertThat(user.isActive()).isFalse();
    }

    @Test
    public void shouldBeActiveAgainWhenReactivatedBySomeoneElse() {

        // given
        var user = userWithId();
        user.updateActiveState(false, OTHER_USER_ID);

        // when
        user.updateActiveState(true, OTHER_USER_ID);

        // then
        assertThat(user.isActive()).isTrue();
    }

    @Test
    public void shouldThrowExceptionWhenTheUserChangesTheirOwnActiveState() {

        // given
        var user = userWithId();

        // then
        assertThatThrownBy(() -> user.updateActiveState(false, user.getPublicId()))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    public void shouldThrowExceptionWhenTheUserChangesTheirOwnRole() {

        // given
        var user = userWithId();

        // then
        assertThatThrownBy(() -> user.updateRole(Role.ADMIN, user.getPublicId()))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    public void shouldChangeTheRoleWhenSomeoneElseChangesIt() {

        // given
        var user = userWithId();

        // when
        user.updateRole(Role.ADMIN, OTHER_USER_ID);

        // then
        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    public void shouldAllowTheUserToResubmitTheirOwnUnchangedRoleAndActiveState() {

        // given
        var user = userWithId();

        // when
        user.updateRole(user.getRole(), user.getPublicId());
        user.updateActiveState(user.isActive(), user.getPublicId());

        // then
        assertThat(user.getRole()).isEqualTo(Role.PLAYER);
        assertThat(user.isActive()).isTrue();
    }

    @Test
    public void shouldStoreTheBioWhenItIsWithinTheLimit() {

        // given
        var user = userWithId();

        // when
        user.updateBio("A wandering bard.");

        // then
        assertThat(user.getBio()).isEqualTo("A wandering bard.");
    }

    @Test
    public void shouldStoreTheBioWhenItIsExactlyAtTheLimit() {

        // given
        var user = userWithId();
        var bio = "a".repeat(2000);

        // when
        user.updateBio(bio);

        // then
        assertThat(user.getBio()).hasSize(2000);
    }

    @Test
    public void shouldThrowExceptionWhenTheBioExceedsTheLimit() {

        // given
        var user = userWithId();
        var bio = "a".repeat(2001);

        // then
        assertThatThrownBy(() -> user.updateBio(bio))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    public void shouldClearTheBioWhenItIsSetToNull() {

        // given
        var user = userWithId();
        user.updateBio("A wandering bard.");

        // when
        user.updateBio(null);

        // then
        assertThat(user.getBio()).isNull();
    }

    private User userWithId() {

        var user = UserFixture.player().build();
        ReflectionTestUtils.setField(user, "id", NUMERIC_ID);
        ReflectionTestUtils.setField(user, "publicId", UUID.randomUUID());

        return user;
    }
}
