package me.moirai.storyengine.core.application.usecase.discord.userdetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.core.application.usecase.discord.userdetails.request.RefreshSessionToken;
import me.moirai.storyengine.core.port.DiscordAuthenticationPort;
import me.moirai.storyengine.infrastructure.inbound.rest.response.DiscordAuthResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

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
        RefreshSessionToken request = RefreshSessionToken.build(token);

        // Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> handler.handle(request));
    }

    @Test
    public void refreshToken_whenValidRequest_thenRefreshToken() {

        // Given
        String token = "TOKEN";
        RefreshSessionToken request = RefreshSessionToken.build(token);

        DiscordAuthResponse authResponse = DiscordAuthResponse.builder()
                .accessToken("token")
                .expiresIn(1234L)
                .refreshToken("token")
                .tokenType("type")
                .scope("scope")
                .build();

        when(discordAuthenticationPort.refreshSessionToken(any())).thenReturn(Mono.just(authResponse));

        // Then
        StepVerifier.create(handler.handle(request))
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result.getAccessToken()).isNotNull().isNotEmpty();
                    assertThat(result.getExpiresIn()).isNotNull();
                    assertThat(result.getRefreshToken()).isNotNull().isNotEmpty();
                    assertThat(result.getScope()).isNotNull().isNotEmpty();
                    assertThat(result.getTokenType()).isNotNull().isNotEmpty();
                })
                .verifyComplete();
    }
}
