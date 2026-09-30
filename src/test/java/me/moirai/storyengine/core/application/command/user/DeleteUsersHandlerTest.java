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

    private static final String REQUESTER_USERNAME = "Merlin";

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
        var first = userWith("first.player");
        var second = userWith("second.player");
        var command = new DeleteUsers(List.of(first.getUsername(), second.getUsername()), REQUESTER_USERNAME);

        when(repository.findByUsername(first.getUsername())).thenReturn(Optional.of(first));
        when(repository.findByUsername(second.getUsername())).thenReturn(Optional.of(second));

        // when
        var result = handler.handle(command);

        // then
        assertThat(result.failedUsernames()).isEmpty();

        verify(repository).delete(first);
        verify(repository).delete(second);
        verify(eventPublisher, times(2)).publishEvent(any(UserDeletedEvent.class));
    }

    @Test
    public void shouldDeleteNothingWhenTheSelectionIsEmpty() {

        // given
        var command = new DeleteUsers(List.of(), REQUESTER_USERNAME);

        // when
        var result = handler.handle(command);

        // then
        assertThat(result.failedUsernames()).isEmpty();

        verify(repository, never()).delete(any(User.class));
        verify(transactionManager, never()).getTransaction(any(TransactionDefinition.class));
    }

    @Test
    public void shouldDeleteNothingWhenTheSelectionIsNull() {

        // given
        var command = new DeleteUsers(null, REQUESTER_USERNAME);

        // when
        var result = handler.handle(command);

        // then
        assertThat(result.failedUsernames()).isEmpty();

        verify(repository, never()).delete(any(User.class));
    }

    @Test
    public void shouldDeleteNothingWhenTheRequesterIsInTheSelection() {

        // given
        var command = new DeleteUsers(List.of("other.player", REQUESTER_USERNAME), REQUESTER_USERNAME);

        // then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(BusinessRuleViolationException.class);

        verify(repository, never()).delete(any(User.class));
        verify(transactionManager, never()).getTransaction(any(TransactionDefinition.class));
    }

    @Test
    public void shouldDeleteNothingWhenTheRequesterIsInTheSelectionInAnotherCase() {

        // given
        var command = new DeleteUsers(List.of("other.player", "mERLIN"), REQUESTER_USERNAME);

        // then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(BusinessRuleViolationException.class);

        verify(repository, never()).delete(any(User.class));
        verify(transactionManager, never()).getTransaction(any(TransactionDefinition.class));
    }

    @Test
    public void shouldReportTheUnknownHandleAsSentAndDeleteTheRestWhenOneUserIsUnknown() {

        // given
        var known = userWith("known.player");
        var command = new DeleteUsers(List.of(known.getUsername(), "Ghost.Player"), REQUESTER_USERNAME);

        when(repository.findByUsername(known.getUsername())).thenReturn(Optional.of(known));
        when(repository.findByUsername("Ghost.Player")).thenReturn(Optional.empty());

        // when
        var result = handler.handle(command);

        // then
        assertThat(result.failedUsernames()).containsExactly("Ghost.Player");

        verify(repository).delete(known);
    }

    @Test
    public void shouldReportTheFailureAndDeleteTheRestWhenOneDeletionThrows() {

        // given
        var failing = userWith("failing.player");
        var succeeding = userWith("succeeding.player");
        var command = new DeleteUsers(List.of(failing.getUsername(), succeeding.getUsername()), REQUESTER_USERNAME);

        when(repository.findByUsername(failing.getUsername())).thenReturn(Optional.of(failing));
        when(repository.findByUsername(succeeding.getUsername())).thenReturn(Optional.of(succeeding));

        doThrow(new IllegalStateException("boom")).when(repository).delete(failing);

        // when
        var result = handler.handle(command);

        // then
        assertThat(result.failedUsernames()).containsExactly(failing.getUsername());

        verify(repository).delete(succeeding);
    }

    @Test
    public void shouldReportTheDatabaseCasingWhenADeletionRequestedInAnotherCaseFails() {

        // given
        var failing = userWith("Failing.Player");
        var command = new DeleteUsers(List.of("fAILING.pLAYER"), REQUESTER_USERNAME);

        when(repository.findByUsername("fAILING.pLAYER")).thenReturn(Optional.of(failing));

        doThrow(new IllegalStateException("boom")).when(repository).delete(failing);

        // when
        var result = handler.handle(command);

        // then
        assertThat(result.failedUsernames()).containsExactly("Failing.Player");
    }

    @Test
    public void shouldDeleteOnceWhenTheSameHandleIsSubmittedTwice() {

        // given
        var user = userWith("john.doe");
        var command = new DeleteUsers(List.of("john.doe", "JOHN.DOE"), REQUESTER_USERNAME);

        when(repository.findByUsername("john.doe")).thenReturn(Optional.of(user));

        // when
        var result = handler.handle(command);

        // then
        assertThat(result.failedUsernames()).isEmpty();

        verify(repository).delete(user);
    }

    @Test
    public void shouldReportFailuresInTheOrderTheyWereSubmittedWhenSeveralFail() {

        // given
        var command = new DeleteUsers(List.of("first.ghost", "second.ghost"), REQUESTER_USERNAME);

        when(repository.findByUsername("first.ghost")).thenReturn(Optional.empty());
        when(repository.findByUsername("second.ghost")).thenReturn(Optional.empty());

        // when
        var result = handler.handle(command);

        // then
        assertThat(result.failedUsernames()).containsExactly("first.ghost", "second.ghost");
    }

    @Test
    public void shouldThrowExceptionWhenTheCommandIsNull() {

        // then
        assertThatThrownBy(() -> handler.handle(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private User userWith(String username) {

        var user = UserFixture.player().username(username).build();
        ReflectionTestUtils.setField(user, "id", 1L);

        return user;
    }
}
