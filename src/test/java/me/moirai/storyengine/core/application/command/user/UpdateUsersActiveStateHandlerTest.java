package me.moirai.storyengine.core.application.command.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.exception.BusinessRuleViolationException;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.domain.userdetails.User;
import me.moirai.storyengine.core.domain.userdetails.UserFixture;
import me.moirai.storyengine.core.port.inbound.userdetails.UpdateUsersActiveState;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UpdateUsersActiveStateHandlerTest {

    private static final String REQUESTER_USERNAME = "requesting.admin";

    @Mock
    private UserRepository repository;

    @InjectMocks
    private UpdateUsersActiveStateHandler handler;

    @Test
    public void shouldChangeNothingWhenTheSelectionIsEmpty() {

        // given
        var command = new UpdateUsersActiveState(List.of(), false, REQUESTER_USERNAME);

        when(repository.findAllByUsernameIn(anyList())).thenReturn(List.of());

        // when
        handler.handle(command);

        // then
        verify(repository, never()).save(any(User.class));
    }

    @Test
    public void shouldChangeNothingWhenTheSelectionIsNull() {

        // given
        var command = new UpdateUsersActiveState(null, false, REQUESTER_USERNAME);

        when(repository.findAllByUsernameIn(anyList())).thenReturn(List.of());

        // when
        handler.handle(command);

        // then
        verify(repository, never()).save(any(User.class));
    }

    @Test
    public void shouldThrowExceptionWhenAnyRequestedUserIsNotFound() {

        // given
        var found = userWith("found.player");
        var command = new UpdateUsersActiveState(List.of(found.getUsername(), "ghost.player"), false,
                REQUESTER_USERNAME);

        when(repository.findAllByUsernameIn(anyList())).thenReturn(List.of(found));

        // then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(NotFoundException.class);

        verify(repository, never()).save(any(User.class));
    }

    @Test
    public void shouldDeactivateEveryUserInTheSelectionWhenTheStateIsFalse() {

        // given
        var first = userWith("first.player");
        var second = userWith("second.player");
        var command = new UpdateUsersActiveState(
                List.of(first.getUsername(), second.getUsername()), false, REQUESTER_USERNAME);

        when(repository.findAllByUsernameIn(anyList())).thenReturn(List.of(first, second));

        // when
        handler.handle(command);

        // then
        assertThat(first.isActive()).isFalse();
        assertThat(second.isActive()).isFalse();

        verify(repository).save(first);
        verify(repository).save(second);
    }

    @Test
    public void shouldReactivateEveryUserInTheSelectionWhenTheStateIsTrue() {

        // given
        var first = userWith("first.player");
        var second = userWith("second.player");
        first.updateActiveState(false, REQUESTER_USERNAME);
        second.updateActiveState(false, REQUESTER_USERNAME);

        var command = new UpdateUsersActiveState(
                List.of(first.getUsername(), second.getUsername()), true, REQUESTER_USERNAME);

        when(repository.findAllByUsernameIn(anyList())).thenReturn(List.of(first, second));

        // when
        handler.handle(command);

        // then
        assertThat(first.isActive()).isTrue();
        assertThat(second.isActive()).isTrue();
    }

    @Test
    public void shouldLeaveUsersAlreadyInTheTargetStateUntouchedWhenTheSelectionIsMixed() {

        // given
        var active = userWith("active.player");
        var inactive = userWith("inactive.player");
        inactive.updateActiveState(false, REQUESTER_USERNAME);

        var command = new UpdateUsersActiveState(
                List.of(active.getUsername(), inactive.getUsername()), false, REQUESTER_USERNAME);

        when(repository.findAllByUsernameIn(anyList())).thenReturn(List.of(active, inactive));

        // when
        handler.handle(command);

        // then
        assertThat(active.isActive()).isFalse();
        assertThat(inactive.isActive()).isFalse();
    }

    @Test
    public void shouldNotReportAMissingUserWhenTwoCasingsOfOneHandleAreSubmitted() {

        // given
        var user = userWith("Merlin");
        var command = new UpdateUsersActiveState(List.of("Merlin", "merlin"), false, REQUESTER_USERNAME);

        when(repository.findAllByUsernameIn(anyList())).thenReturn(List.of(user));

        // when
        handler.handle(command);

        // then
        assertThat(user.isActive()).isFalse();

        verify(repository).save(user);
    }

    @Test
    public void shouldNotSaveWhenTheRequesterChangesTheirOwnActiveState() {

        // given
        var self = userWith(REQUESTER_USERNAME);
        var command = new UpdateUsersActiveState(List.of(REQUESTER_USERNAME), false, REQUESTER_USERNAME);

        when(repository.findAllByUsernameIn(anyList())).thenReturn(List.of(self));

        // then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(BusinessRuleViolationException.class);

        verify(repository, never()).save(any(User.class));
    }

    @Test
    public void shouldThrowExceptionWhenTheCommandIsNull() {

        // then
        assertThatThrownBy(() -> handler.handle(null))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(repository);
    }

    private User userWith(String username) {

        return UserFixture.player().username(username).build();
    }
}
