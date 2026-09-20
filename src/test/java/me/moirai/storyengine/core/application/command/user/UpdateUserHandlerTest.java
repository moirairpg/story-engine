package me.moirai.storyengine.core.application.command.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.enums.Role;
import me.moirai.storyengine.common.exception.BusinessRuleViolationException;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.domain.userdetails.User;
import me.moirai.storyengine.core.domain.userdetails.UserFixture;
import me.moirai.storyengine.core.port.inbound.userdetails.UpdateUser;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UpdateUserHandlerTest {

    private static final UUID REQUESTER_ID = UUID.fromString("00000000-0000-0000-0000-0000000000aa");

    @Mock
    private UserRepository repository;

    @InjectMocks
    private UpdateUserHandler handler;

    @Test
    public void shouldThrowExceptionWhenUserIdIsNull() {

        // given
        var command = new UpdateUser(null, Role.ADMIN, true, null, REQUESTER_ID);

        // then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldThrowExceptionWhenRoleIsNull() {

        // given
        var command = new UpdateUser(UUID.randomUUID(), null, true, null, REQUESTER_ID);

        // then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldThrowExceptionWhenUserIsNotFound() {

        // given
        var userId = UUID.randomUUID();
        var command = new UpdateUser(userId, Role.ADMIN, true, null, REQUESTER_ID);

        when(repository.findByPublicId(userId)).thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    public void shouldApplyEveryFieldWhenTheAccountIsUpdated() {

        // given
        var userId = UUID.randomUUID();
        var user = UserFixture.playerWithId();
        var command = new UpdateUser(userId, Role.ADMIN, false, "A wandering bard.", REQUESTER_ID);

        when(repository.findByPublicId(userId)).thenReturn(Optional.of(user));

        // when
        handler.handle(command);

        // then
        verify(repository).save(user);

        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
        assertThat(user.isActive()).isFalse();
        assertThat(user.getBio()).isEqualTo("A wandering bard.");
    }

    @Test
    public void shouldReactivateTheAccountWhenActiveStateIsTrue() {

        // given
        var userId = UUID.randomUUID();
        var user = UserFixture.playerWithId();
        user.updateActiveState(false, REQUESTER_ID);

        var command = new UpdateUser(userId, Role.PLAYER, true, null, REQUESTER_ID);

        when(repository.findByPublicId(userId)).thenReturn(Optional.of(user));

        // when
        handler.handle(command);

        // then
        assertThat(user.isActive()).isTrue();
    }

    @Test
    public void shouldClearTheBioWhenTheSubmittedBioIsNull() {

        // given
        var userId = UUID.randomUUID();
        var user = UserFixture.playerWithId();
        user.updateBio("A wandering bard.");

        var command = new UpdateUser(userId, Role.PLAYER, true, null, REQUESTER_ID);

        when(repository.findByPublicId(userId)).thenReturn(Optional.of(user));

        // when
        handler.handle(command);

        // then
        assertThat(user.getBio()).isNull();
    }

    @Test
    public void shouldNotSaveWhenTheRequesterChangesTheirOwnRole() {

        // given
        var user = UserFixture.playerWithId();
        var command = new UpdateUser(user.getPublicId(), Role.ADMIN, true, null, user.getPublicId());

        when(repository.findByPublicId(user.getPublicId())).thenReturn(Optional.of(user));

        // then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(BusinessRuleViolationException.class);

        verify(repository, never()).save(any(User.class));
    }

    @Test
    public void shouldNotSaveWhenTheRequesterChangesTheirOwnActiveState() {

        // given
        var user = UserFixture.playerWithId();
        var command = new UpdateUser(user.getPublicId(), Role.PLAYER, false, null, user.getPublicId());

        when(repository.findByPublicId(user.getPublicId())).thenReturn(Optional.of(user));

        // then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(BusinessRuleViolationException.class);

        verify(repository, never()).save(any(User.class));
    }

    @Test
    public void shouldSaveWhenTheRequesterEditsOnlyTheirOwnBio() {

        // given
        var user = UserFixture.playerWithId();
        var command = new UpdateUser(user.getPublicId(), user.getRole(), user.isActive(),
                "A wandering bard.", user.getPublicId());

        when(repository.findByPublicId(user.getPublicId())).thenReturn(Optional.of(user));

        // when
        handler.handle(command);

        // then
        verify(repository).save(user);

        assertThat(user.getBio()).isEqualTo("A wandering bard.");
    }

    @Test
    public void shouldNotSaveWhenTheBioIsRejected() {

        // given
        var userId = UUID.randomUUID();
        var command = new UpdateUser(userId, Role.PLAYER, true, "a".repeat(2001), REQUESTER_ID);

        when(repository.findByPublicId(userId)).thenReturn(Optional.of(UserFixture.playerWithId()));

        // then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(BusinessRuleViolationException.class);

        verify(repository, never()).save(any(User.class));
    }
}
