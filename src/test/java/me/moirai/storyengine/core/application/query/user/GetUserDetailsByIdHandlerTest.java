package me.moirai.storyengine.core.application.query.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.enums.Role;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.common.exception.RestException;
import me.moirai.storyengine.core.port.inbound.userdetails.GetUserDetailsById;
import me.moirai.storyengine.core.port.inbound.userdetails.UserData;
import me.moirai.storyengine.core.port.outbound.discord.DiscordUserDataResponse;
import me.moirai.storyengine.core.port.outbound.discord.DiscordUserDetailsPort;
import me.moirai.storyengine.core.port.outbound.userdetails.UserReader;

@ExtendWith(MockitoExtension.class)
public class GetUserDetailsByIdHandlerTest {

    @Mock
    private UserReader userReader;

    @Mock
    private DiscordUserDetailsPort discordUserDetailsPort;

    @InjectMocks
    private GetUserDetailsByIdHandler handler;

    @Test
    public void retrieveUser_whenUserIsFound_thenReturnUserData() {

        // Given
        var userId = UUID.randomUUID();
        var query = new GetUserDetailsById(userId);
        var userData = new UserData(userId, 12345L, "1234", "john.natalis", Role.PLAYER, true, null,
                Instant.now());
        var userDetails = new DiscordUserDataResponse("1234", "john.natalis", null, null, null, null, null);

        when(userReader.getUserById(any(UUID.class))).thenReturn(Optional.of(userData));
        when(discordUserDetailsPort.getUserById(anyString())).thenReturn(Optional.of(userDetails));

        // When
        var result = handler.handle(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.discordId()).isEqualTo(userData.discordId());
        assertThat(result.discordUsername()).isEqualTo("john.natalis");
        assertThat(result.username()).isEqualTo(userData.username());
    }

    @Test
    public void retrieveUser_whenUserNotExistsInMoirai_thenThrowException() {

        // Given
        var expectedMessage = "The User with the requested ID is not registered in MoirAI";
        var userId = UUID.randomUUID();
        var query = new GetUserDetailsById(userId);

        when(userReader.getUserById(any(UUID.class))).thenReturn(Optional.empty());

        // Then
        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> handler.execute(query))
                .withMessage(expectedMessage);
    }

    @Test
    public void retrieveUser_whenUserNotExistsInDiscord_thenThrowException() {

        // Given
        var expectedMessage = "The Discord User with the requested ID does not exist";
        var userId = UUID.randomUUID();
        var query = new GetUserDetailsById(userId);
        var userData = new UserData(userId, 12345L, "1234", "john.natalis", Role.PLAYER, true, null,
                Instant.now());

        when(userReader.getUserById(any(UUID.class))).thenReturn(Optional.of(userData));
        when(discordUserDetailsPort.getUserById(anyString())).thenReturn(Optional.empty());

        // Then
        assertThatExceptionOfType(RestException.class)
                .isThrownBy(() -> handler.execute(query))
                .withMessage(expectedMessage);
    }

    @Test
    public void retrieveUser_whenUserIdIsNull_thenThrowException() {

        // Given
        var expectedMessage = "User ID cannot be null";
        var query = new GetUserDetailsById(null);

        // Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> handler.handle(query))
                .withMessage(expectedMessage);
    }
}
