package me.moirai.storyengine.core.application.command.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

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

    private static final String USERNAME = "john.doe";
    private static final String DISPLAY_NAME = "John Doe";
    private static final String REQUESTER_USERNAME = "requesting.admin";

    @Mock
    private UserRepository repository;

    @InjectMocks
    private UpdateUserHandler handler;

    @Test
    public void shouldThrowExceptionWhenUserIsNotFound() {

        // given
        var command = new UpdateUser("ghost", Role.ADMIN, true, null, DISPLAY_NAME, REQUESTER_USERNAME);

        when(repository.findByUsername("ghost")).thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    public void shouldApplyEveryFieldWhenTheAccountIsUpdated() {

        // given
        var user = UserFixture.playerWithId();
        var command = new UpdateUser(USERNAME, Role.ADMIN, false, "A wandering bard.", "Merlin the Grey",
                REQUESTER_USERNAME);

        when(repository.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        // when
        handler.handle(command);

        // then
        verify(repository).save(user);

        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
        assertThat(user.isActive()).isFalse();
        assertThat(user.getBio()).isEqualTo("A wandering bard.");
        assertThat(user.getDisplayName()).isEqualTo("Merlin the Grey");
    }

    @Test
    public void shouldReactivateTheAccountWhenActiveStateIsTrue() {

        // given
        var user = UserFixture.playerWithId();
        user.updateActiveState(false, REQUESTER_USERNAME);

        var command = new UpdateUser(USERNAME, Role.PLAYER, true, null, DISPLAY_NAME, REQUESTER_USERNAME);

        when(repository.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        // when
        handler.handle(command);

        // then
        assertThat(user.isActive()).isTrue();
    }

    @Test
    public void shouldClearTheBioWhenTheSubmittedBioIsNull() {

        // given
        var user = UserFixture.playerWithId();
        user.updateBio("A wandering bard.");

        var command = new UpdateUser(USERNAME, Role.PLAYER, true, null, DISPLAY_NAME, REQUESTER_USERNAME);

        when(repository.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        // when
        handler.handle(command);

        // then
        assertThat(user.getBio()).isNull();
    }

    @Test
    public void shouldNotSaveWhenTheRequesterChangesTheirOwnRole() {

        // given
        var user = UserFixture.playerWithId();
        var command = new UpdateUser(user.getUsername(), Role.ADMIN, true, null, DISPLAY_NAME, user.getUsername());

        when(repository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        // then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(BusinessRuleViolationException.class);

        verify(repository, never()).save(any(User.class));
    }

    @Test
    public void shouldNotSaveWhenTheRequesterChangesTheirOwnActiveState() {

        // given
        var user = UserFixture.playerWithId();
        var command = new UpdateUser(user.getUsername(), Role.PLAYER, false, null, DISPLAY_NAME, user.getUsername());

        when(repository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        // then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(BusinessRuleViolationException.class);

        verify(repository, never()).save(any(User.class));
    }

    @Test
    public void shouldSaveWhenTheRequesterEditsOnlyTheirOwnBio() {

        // given
        var user = UserFixture.playerWithId();
        var command = new UpdateUser(user.getUsername(), user.getRole(), user.isActive(),
                "A wandering bard.", user.getDisplayName(), user.getUsername());

        when(repository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        // when
        handler.handle(command);

        // then
        verify(repository).save(user);

        assertThat(user.getBio()).isEqualTo("A wandering bard.");
    }

    @Test
    public void shouldNotSaveWhenTheBioIsRejected() {

        // given
        var command = new UpdateUser(USERNAME, Role.PLAYER, true, "a".repeat(2001), DISPLAY_NAME, REQUESTER_USERNAME);

        when(repository.findByUsername(USERNAME)).thenReturn(Optional.of(UserFixture.playerWithId()));

        // then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(BusinessRuleViolationException.class);

        verify(repository, never()).save(any(User.class));
    }
}
