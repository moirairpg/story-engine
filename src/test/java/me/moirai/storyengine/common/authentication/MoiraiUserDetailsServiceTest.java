package me.moirai.storyengine.common.authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import me.moirai.storyengine.common.exception.AuthenticationFailedException;
import me.moirai.storyengine.common.security.authentication.MoiraiPrincipal;
import me.moirai.storyengine.common.security.authentication.MoiraiUserDetailsService;
import me.moirai.storyengine.core.port.inbound.userdetails.UserData;
import me.moirai.storyengine.core.port.outbound.discord.DiscordAuthenticationPort;
import me.moirai.storyengine.core.port.outbound.discord.DiscordUserDataResponse;
import me.moirai.storyengine.core.port.outbound.userdetails.UserReader;

@ExtendWith(MockitoExtension.class)
public class MoiraiUserDetailsServiceTest {

    @Mock
    private DiscordAuthenticationPort discordAuthenticationPort;

    @Mock
    private UserReader userReader;

    @InjectMocks
    private MoiraiUserDetailsService service;

    @Test
    public void authenticateUser_whenUserExists_thenReturnPrincipal() {

        // Given
        var token = "AUTH_TOKEN / REFRESH_TOKEN";
        var username = "john.doe";
        var publicId = UUID.randomUUID();

        var response = new DiscordUserDataResponse(
                "12345",
                username,
                null,
                null,
                "email@email.com",
                null,
                null);

        var userData = new UserData(publicId, 1L, "12345", "john.doe", Role.PLAYER, true, null,
                Instant.now());

        when(discordAuthenticationPort.getLoggedUser(anyString())).thenReturn(response);
        when(userReader.getUserByDiscordId(anyString())).thenReturn(Optional.of(userData));

        // When
        var userDetails = service.loadUserByUsername(token);

        // Then
        var principal = (MoiraiPrincipal) userDetails;
        assertThat(principal).isNotNull();
        assertThat(principal.getUsername()).isEqualTo(response.username());
        assertThat(principal.email()).isEqualTo(response.email());
        assertThat(principal.authorizationToken()).isEqualTo("AUTH_TOKEN");
        assertThat(principal.refreshToken()).isEqualTo("REFRESH_TOKEN");
    }

    @Test
    public void authenticateUser_whenUserIsDeactivated_thenThrowAuthenticationFailed() {

        // Given
        var token = "AUTH_TOKEN / REFRESH_TOKEN";
        var publicId = UUID.randomUUID();

        var response = new DiscordUserDataResponse(
                "12345",
                "john.doe",
                null,
                null,
                "email@email.com",
                null,
                null);

        var userData = new UserData(publicId, 1L, "12345", "john.doe", Role.PLAYER, false, null,
                Instant.now());

        when(discordAuthenticationPort.getLoggedUser(anyString())).thenReturn(response);
        when(userReader.getUserByDiscordId(anyString())).thenReturn(Optional.of(userData));

        // Then
        assertThatThrownBy(() -> service.loadUserByUsername(token))
                .isInstanceOf(AuthenticationFailedException.class);
    }
}
