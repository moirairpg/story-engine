package me.moirai.storyengine.core.application.command.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import me.moirai.storyengine.common.exception.BusinessRuleViolationException;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.domain.userdetails.User;
import me.moirai.storyengine.core.domain.userdetails.UserFixture;
import me.moirai.storyengine.core.port.inbound.userdetails.UpdateUsersActiveState;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UpdateUsersActiveStateHandlerTest {

    private static final UUID REQUESTER_ID = UUID.fromString("00000000-0000-0000-0000-0000000000aa");

    @Mock
    private UserRepository repository;

    @InjectMocks
    private UpdateUsersActiveStateHandler handler;

    @Test
    public void shouldChangeNothingWhenTheSelectionIsEmpty() {

        // given
        var command = new UpdateUsersActiveState(List.of(), false, REQUESTER_ID);

        when(repository.findAllByPublicIdIn(anyCollection())).thenReturn(List.of());

        // when
        handler.handle(command);

        // then
        verify(repository, never()).save(any(User.class));
    }

    @Test
    public void shouldChangeNothingWhenTheSelectionIsNull() {

        // given
        var command = new UpdateUsersActiveState(null, false, REQUESTER_ID);

        when(repository.findAllByPublicIdIn(anyCollection())).thenReturn(List.of());

        // when
        handler.handle(command);

        // then
        verify(repository, never()).save(any(User.class));
    }

    @Test
    public void shouldThrowExceptionWhenAnyRequestedUserIsNotFound() {

        // given
        var found = userWith(UUID.randomUUID());
        var command = new UpdateUsersActiveState(List.of(found.getPublicId(), UUID.randomUUID()), false, REQUESTER_ID);

        when(repository.findAllByPublicIdIn(anyCollection())).thenReturn(List.of(found));

        // then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(NotFoundException.class);

        verify(repository, never()).save(any(User.class));
    }

    @Test
    public void shouldDeactivateEveryUserInTheSelectionWhenTheStateIsFalse() {

        // given
        var first = userWith(UUID.randomUUID());
        var second = userWith(UUID.randomUUID());
        var command = new UpdateUsersActiveState(
                List.of(first.getPublicId(), second.getPublicId()), false, REQUESTER_ID);

        when(repository.findAllByPublicIdIn(anyCollection())).thenReturn(List.of(first, second));

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
        var first = userWith(UUID.randomUUID());
        var second = userWith(UUID.randomUUID());
        first.updateActiveState(false, REQUESTER_ID);
        second.updateActiveState(false, REQUESTER_ID);

        var command = new UpdateUsersActiveState(
                List.of(first.getPublicId(), second.getPublicId()), true, REQUESTER_ID);

        when(repository.findAllByPublicIdIn(anyCollection())).thenReturn(List.of(first, second));

        // when
        handler.handle(command);

        // then
        assertThat(first.isActive()).isTrue();
        assertThat(second.isActive()).isTrue();
    }

    @Test
    public void shouldLeaveUsersAlreadyInTheTargetStateUntouchedWhenTheSelectionIsMixed() {

        // given
        var active = userWith(UUID.randomUUID());
        var inactive = userWith(UUID.randomUUID());
        inactive.updateActiveState(false, REQUESTER_ID);

        var command = new UpdateUsersActiveState(
                List.of(active.getPublicId(), inactive.getPublicId()), false, REQUESTER_ID);

        when(repository.findAllByPublicIdIn(anyCollection())).thenReturn(List.of(active, inactive));

        // when
        handler.handle(command);

        // then
        assertThat(active.isActive()).isFalse();
        assertThat(inactive.isActive()).isFalse();
    }

    @Test
    public void shouldDeduplicateWhenTheSameIdIsSubmittedTwice() {

        // given
        var user = userWith(UUID.randomUUID());
        var command = new UpdateUsersActiveState(
                List.of(user.getPublicId(), user.getPublicId()), false, REQUESTER_ID);

        when(repository.findAllByPublicIdIn(anyCollection())).thenReturn(List.of(user));

        // when
        handler.handle(command);

        // then
        assertThat(user.isActive()).isFalse();
    }

    @Test
    public void shouldNotSaveWhenTheRequesterChangesTheirOwnActiveState() {

        // given
        var self = userWith(REQUESTER_ID);
        var command = new UpdateUsersActiveState(List.of(REQUESTER_ID), false, REQUESTER_ID);

        when(repository.findAllByPublicIdIn(anyCollection())).thenReturn(List.of(self));

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

    private User userWith(UUID publicId) {

        var user = UserFixture.player().build();
        ReflectionTestUtils.setField(user, "publicId", publicId);

        return user;
    }
}
