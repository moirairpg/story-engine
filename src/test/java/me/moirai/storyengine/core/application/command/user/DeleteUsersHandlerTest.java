package me.moirai.storyengine.core.application.command.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.SimpleTransactionStatus;

import me.moirai.storyengine.common.exception.BusinessRuleViolationException;
import me.moirai.storyengine.core.domain.userdetails.User;
import me.moirai.storyengine.core.domain.userdetails.UserDeletedEvent;
import me.moirai.storyengine.core.domain.userdetails.UserFixture;
import me.moirai.storyengine.core.port.inbound.userdetails.DeleteUsers;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

@ExtendWith(MockitoExtension.class)
public class DeleteUsersHandlerTest {

    private static final UUID REQUESTER_ID = UUID.fromString("00000000-0000-0000-0000-0000000000aa");

    @Mock
    private UserRepository repository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private PlatformTransactionManager transactionManager;

    private DeleteUsersHandler handler;

    @BeforeEach
    public void before() {

        lenient().when(transactionManager.getTransaction(any(TransactionDefinition.class)))
                .thenReturn(new SimpleTransactionStatus());

        handler = new DeleteUsersHandler(repository, eventPublisher, transactionManager);
    }

    @Test
    public void shouldDeleteEveryUserInTheSelectionWhenAllExist() {

        // given
        var first = userWith(UUID.randomUUID());
        var second = userWith(UUID.randomUUID());
        var command = new DeleteUsers(List.of(first.getPublicId(), second.getPublicId()), REQUESTER_ID);

        when(repository.findByPublicId(first.getPublicId())).thenReturn(Optional.of(first));
        when(repository.findByPublicId(second.getPublicId())).thenReturn(Optional.of(second));

        // when
        var result = handler.handle(command);

        // then
        assertThat(result.failedUserIds()).isEmpty();

        verify(repository).delete(first);
        verify(repository).delete(second);
        verify(eventPublisher, times(2)).publishEvent(any(UserDeletedEvent.class));
    }

    @Test
    public void shouldDeleteNothingWhenTheSelectionIsEmpty() {

        // given
        var command = new DeleteUsers(List.of(), REQUESTER_ID);

        // when
        var result = handler.handle(command);

        // then
        assertThat(result.failedUserIds()).isEmpty();

        verify(repository, never()).delete(any(User.class));
        verify(transactionManager, never()).getTransaction(any(TransactionDefinition.class));
    }

    @Test
    public void shouldDeleteNothingWhenTheSelectionIsNull() {

        // given
        var command = new DeleteUsers(null, REQUESTER_ID);

        // when
        var result = handler.handle(command);

        // then
        assertThat(result.failedUserIds()).isEmpty();

        verify(repository, never()).delete(any(User.class));
    }

    @Test
    public void shouldDeleteNothingWhenTheRequesterIsInTheSelection() {

        // given
        var other = UUID.randomUUID();
        var command = new DeleteUsers(List.of(other, REQUESTER_ID), REQUESTER_ID);

        // then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(BusinessRuleViolationException.class);

        verify(repository, never()).delete(any(User.class));
        verify(transactionManager, never()).getTransaction(any(TransactionDefinition.class));
    }

    @Test
    public void shouldReportTheFailureAndDeleteTheRestWhenOneUserIsUnknown() {

        // given
        var known = userWith(UUID.randomUUID());
        var unknown = UUID.randomUUID();
        var command = new DeleteUsers(List.of(known.getPublicId(), unknown), REQUESTER_ID);

        when(repository.findByPublicId(known.getPublicId())).thenReturn(Optional.of(known));
        when(repository.findByPublicId(unknown)).thenReturn(Optional.empty());

        // when
        var result = handler.handle(command);

        // then
        assertThat(result.failedUserIds()).containsExactly(unknown);

        verify(repository).delete(known);
    }

    @Test
    public void shouldReportTheFailureAndDeleteTheRestWhenOneDeletionThrows() {

        // given
        var failing = userWith(UUID.randomUUID());
        var succeeding = userWith(UUID.randomUUID());
        var command = new DeleteUsers(List.of(failing.getPublicId(), succeeding.getPublicId()), REQUESTER_ID);

        when(repository.findByPublicId(failing.getPublicId())).thenReturn(Optional.of(failing));
        when(repository.findByPublicId(succeeding.getPublicId())).thenReturn(Optional.of(succeeding));

        doThrow(new IllegalStateException("boom")).when(repository).delete(failing);

        // when
        var result = handler.handle(command);

        // then
        assertThat(result.failedUserIds()).containsExactly(failing.getPublicId());

        verify(repository).delete(succeeding);
    }

    @Test
    public void shouldDeleteOnceWhenTheSameIdIsSubmittedTwice() {

        // given
        var user = userWith(UUID.randomUUID());
        var command = new DeleteUsers(List.of(user.getPublicId(), user.getPublicId()), REQUESTER_ID);

        when(repository.findByPublicId(user.getPublicId())).thenReturn(Optional.of(user));

        // when
        var result = handler.handle(command);

        // then
        assertThat(result.failedUserIds()).isEmpty();

        verify(repository).delete(user);
    }

    @Test
    public void shouldReportFailuresInTheOrderTheyWereSubmittedWhenSeveralFail() {

        // given
        var firstUnknown = UUID.randomUUID();
        var secondUnknown = UUID.randomUUID();
        var command = new DeleteUsers(List.of(firstUnknown, secondUnknown), REQUESTER_ID);

        when(repository.findByPublicId(firstUnknown)).thenReturn(Optional.empty());
        when(repository.findByPublicId(secondUnknown)).thenReturn(Optional.empty());

        // when
        var result = handler.handle(command);

        // then
        assertThat(result.failedUserIds()).containsExactly(firstUnknown, secondUnknown);
    }

    @Test
    public void shouldThrowExceptionWhenTheCommandIsNull() {

        // then
        assertThatThrownBy(() -> handler.handle(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private User userWith(UUID publicId) {

        var user = UserFixture.player().build();
        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(user, "publicId", publicId);

        return user;
    }
}
