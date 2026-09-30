package me.moirai.storyengine.core.application.command.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.exception.BusinessRuleViolationException;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.domain.userdetails.User;
import me.moirai.storyengine.core.domain.userdetails.UserFixture;
import me.moirai.storyengine.core.port.inbound.userdetails.UpdateUserUsername;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UpdateUsernameHandlerTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private UpdateUsernameHandler handler;

    @Test
    public void shouldUpdateUsernameWhenUserIsFound() {

        // given
        var command = new UpdateUserUsername("john.doe", "new.username");
        var user = UserFixture.playerWithId();

        when(repository.findByUsername("john.doe")).thenReturn(Optional.of(user));
        when(repository.findByUsername("new.username")).thenReturn(Optional.empty());
        when(repository.save(user)).thenReturn(user);

        // when
        handler.handle(command);

        // then
        verify(repository).save(user);

        assertThat(user.getUsername()).isEqualTo("new.username");
    }

    @Test
    public void shouldThrowWhenTheNewUsernameIsAlreadyTaken() {

        // given
        var command = new UpdateUserUsername("john.doe", "Taken.Name");
        var user = UserFixture.playerWithId();
        var holder = UserFixture.player().username("taken.name").build();

        when(repository.findByUsername("john.doe")).thenReturn(Optional.of(user));
        when(repository.findByUsername("Taken.Name")).thenReturn(Optional.of(holder));

        // then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessage("This username is already taken");

        verify(repository, never()).save(any(User.class));
    }

    @Test
    public void shouldThrowWhenUserIsNotFound() {

        // given
        var command = new UpdateUserUsername("ghost", "new.username");

        when(repository.findByUsername(anyString())).thenReturn(Optional.empty());

        // then
        assertThrows(NotFoundException.class, () -> handler.handle(command));
    }
}
