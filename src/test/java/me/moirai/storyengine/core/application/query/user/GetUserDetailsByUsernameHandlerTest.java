package me.moirai.storyengine.core.application.query.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
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
import me.moirai.storyengine.core.port.inbound.userdetails.GetUserDetailsByUsername;
import me.moirai.storyengine.core.port.outbound.discord.DiscordUserDataResponse;
import me.moirai.storyengine.core.port.outbound.discord.DiscordUserDetailsPort;
import me.moirai.storyengine.core.port.outbound.userdetails.UserData;
import me.moirai.storyengine.core.port.outbound.userdetails.UserReader;

@ExtendWith(MockitoExtension.class)
public class GetUserDetailsByUsernameHandlerTest {

    @Mock
    private UserReader userReader;

    @Mock
    private DiscordUserDetailsPort discordUserDetailsPort;

    @InjectMocks
    private GetUserDetailsByUsernameHandler handler;

    @Test
    public void retrieveUser_whenUserIsFound_thenReturnUserData() {

        // Given
        var query = new GetUserDetailsByUsername("john.natalis");
        var userData = new UserData(UUID.randomUUID(), 12345L, "1234", "john.natalis", "John Natalis", Role.PLAYER,
                true, null, Instant.now());
        var userDetails = new DiscordUserDataResponse("1234", "john_discord", null, null, null, null, null);

        when(userReader.getUserByUsername("john.natalis")).thenReturn(Optional.of(userData));
        when(discordUserDetailsPort.getUserById(anyString())).thenReturn(Optional.of(userDetails));

        // When
        var result = handler.handle(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.discordUsername()).isEqualTo("john_discord");
        assertThat(result.username()).isEqualTo(userData.username());
        assertThat(result.displayName()).isEqualTo("John Natalis");
    }

    @Test
    public void retrieveUser_whenUserNotExistsInMoirai_thenThrowException() {

        // Given
        var expectedMessage = "The User with the requested username is not registered in MoirAI";
        var query = new GetUserDetailsByUsername("ghost");

        when(userReader.getUserByUsername(anyString())).thenReturn(Optional.empty());

        // Then
        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> handler.execute(query))
                .withMessage(expectedMessage);
    }

    @Test
    public void retrieveUser_whenUserNotExistsInDiscord_thenThrowException() {

        // Given
        var expectedMessage = "The Discord User with the requested ID does not exist";
        var query = new GetUserDetailsByUsername("john.natalis");
        var userData = new UserData(UUID.randomUUID(), 12345L, "1234", "john.natalis", "John Natalis", Role.PLAYER,
                true, null, Instant.now());

        when(userReader.getUserByUsername(anyString())).thenReturn(Optional.of(userData));
        when(discordUserDetailsPort.getUserById(anyString())).thenReturn(Optional.empty());

        // Then
        assertThatExceptionOfType(RestException.class)
                .isThrownBy(() -> handler.execute(query))
                .withMessage(expectedMessage);
    }
}
