package me.moirai.storyengine.core.application.query.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.core.port.inbound.discord.userdetails.AuthenticateUserResult;
import me.moirai.storyengine.core.port.inbound.discord.userdetails.RefreshSessionToken;
import me.moirai.storyengine.core.port.outbound.discord.DiscordAuthenticationPort;

@ExtendWith(MockitoExtension.class)
public class RefreshSessionTokenHandlerTest {

    @Mock
    private DiscordAuthenticationPort discordAuthenticationPort;

    @InjectMocks
    private RefreshSessionTokenHandler handler;

    @Test
    public void refreshToken_whenRefreshTokenIsNull_thenThrowException() {

        // Given
        String token = null;
        var request = new RefreshSessionToken(token);

        // Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> handler.handle(request));
    }

    @Test
    public void refreshToken_whenValidRequest_thenRefreshToken() {

        // Given
        var token = "TOKEN";
        var request = new RefreshSessionToken(token);

        var authResponse = new AuthenticateUserResult(
                "token",
                1234L,
                "token",
                "type",
                "scope");

        when(discordAuthenticationPort.refreshSessionToken(any())).thenReturn(authResponse);

        // When
        var result = handler.handle(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.accessToken()).isNotNull().isNotEmpty();
        assertThat(result.expiresIn()).isNotNull();
        assertThat(result.refreshToken()).isNotNull().isNotEmpty();
        assertThat(result.scope()).isNotNull().isNotEmpty();
        assertThat(result.tokenType()).isNotNull().isNotEmpty();
    }
}
