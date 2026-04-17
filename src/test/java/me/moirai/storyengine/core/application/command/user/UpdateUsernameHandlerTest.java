package me.moirai.storyengine.core.application.command.user;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.exception.NotFoundException;
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
        var command = new UpdateUserUsername(UserFixture.PUBLIC_ID, "new.username");
        var user = UserFixture.playerWithId();

        when(repository.findByPublicId(UserFixture.PUBLIC_ID)).thenReturn(Optional.of(user));
        when(repository.save(user)).thenReturn(user);

        // when
        handler.handle(command);

        // then
        verify(repository).save(user);
    }

    @Test
    public void shouldThrowWhenUserIsNotFound() {

        // given
        var command = new UpdateUserUsername(UserFixture.PUBLIC_ID, "new.username");

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

        // then
        assertThrows(NotFoundException.class, () -> handler.handle(command));
    }

    @Test
    public void shouldThrowWhenUserIdIsNull() {

        // given
        var command = new UpdateUserUsername(null, "new.username");

        // then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void shouldThrowWhenUsernameIsBlank() {

        // given
        var command = new UpdateUserUsername(UserFixture.PUBLIC_ID, "   ");

        // then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void shouldThrowWhenUsernameIsNull() {

        // given
        var command = new UpdateUserUsername(UserFixture.PUBLIC_ID, null);

        // then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }
}
