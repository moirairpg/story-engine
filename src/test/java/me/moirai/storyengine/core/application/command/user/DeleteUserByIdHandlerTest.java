package me.moirai.storyengine.core.application.command.user;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.core.domain.userdetails.User;
import me.moirai.storyengine.core.domain.userdetails.UserFixture;
import me.moirai.storyengine.core.port.inbound.discord.userdetails.DeleteUserById;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

@ExtendWith(MockitoExtension.class)
public class DeleteUserByIdHandlerTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private DeleteUserByIdHandler handler;

    @Test
    public void deleteUser_whenIdIsNull_thenThrowException() {

        // Given
        DeleteUserById command = new DeleteUserById(null);

        // Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> handler.handle(command));
    }

    @Test
    public void deleteUser_whenUserNotFound_thenThrowException() {

        // Given
        DeleteUserById command = new DeleteUserById(UUID.randomUUID());

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

        // Then
        assertThatExceptionOfType(AssetNotFoundException.class)
                .isThrownBy(() -> handler.handle(command));
    }

    @Test
    public void deleteUser_whenValidRequest_thenUserIsDeleted() {

        // Given
        DeleteUserById command = new DeleteUserById(UUID.randomUUID());
        User user = UserFixture.player().build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(user));

        // When
        handler.handle(command);

        // Then
        verify(repository, times(1)).delete(user);
    }
}
