package me.moirai.storyengine.core.application.usecase.discord.userdetails;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.core.port.inbound.discord.userdetails.DeleteUserByDiscordId;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;
import me.moirai.storyengine.core.domain.userdetails.User;
import me.moirai.storyengine.core.domain.userdetails.UserFixture;

@ExtendWith(MockitoExtension.class)
public class DeleteUserByDiscordIdHandlerTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private DeleteUserByDiscordIdHandler handler;

    @Test
    public void deleteUser_whenIdIsNull_thenThrowException() {

        // Given
        String userId = null;
        DeleteUserByDiscordId command = DeleteUserByDiscordId.build(userId);

        // Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> handler.handle(command));
    }

    @Test
    public void deleteUser_whenUserNotFound_thenThrowException() {

        // Given
        String userId = "123123";
        DeleteUserByDiscordId command = DeleteUserByDiscordId.build(userId);

        when(repository.findByDiscordId(anyString())).thenReturn(Optional.empty());

        // Then
        assertThatExceptionOfType(AssetNotFoundException.class)
                .isThrownBy(() -> handler.handle(command));
    }

    @Test
    public void deleteUser_whenValidRequest_thenUserIsDeleted() {

        // Given
        String userId = "123123";
        DeleteUserByDiscordId command = DeleteUserByDiscordId.build(userId);
        User user = UserFixture.player().build();

        when(repository.findByDiscordId(anyString())).thenReturn(Optional.of(user));

        // When
        handler.handle(command);

        // Then
        verify(repository, times(1)).delete(user);
    }
}
