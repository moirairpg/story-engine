package me.moirai.storyengine.core.application.usecase.discord.userdetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.core.port.inbound.discord.userdetails.AuthenticateUser;
import me.moirai.storyengine.core.port.inbound.discord.userdetails.AuthenticateUserResult;
import me.moirai.storyengine.core.domain.userdetails.User;
import me.moirai.storyengine.core.domain.userdetails.UserFixture;
import me.moirai.storyengine.core.port.outbound.discord.DiscordAuthenticationPort;
import me.moirai.storyengine.core.port.outbound.discord.DiscordUserDataResponse;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class AuthenticateUserHandlerTest {

    @Mock
    private UserRepository repository;

    @Mock
    private DiscordAuthenticationPort discordAuthenticationPort;

    private AuthenticateUserHandler handler;

    @BeforeEach
    public void before() {

        handler = new AuthenticateUserHandler("/someuri", "/someuri",
                "/someuri", repository, discordAuthenticationPort);
    }

    @Test
    public void authenticateUser_whenExchangeCodeIsNull_thenThrowException() {

        // Given
        String expectedMessage = "Authentication code cannot be null";
        String exchangeCode = null;
        AuthenticateUser query = AuthenticateUser.build(exchangeCode);

        // Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> handler.handle(query))
                .withMessage(expectedMessage);
    }

    @Test
    public void authenticateUser_whenDataIsValid_thenUserIsAuthenticated() {

        // Given
        String exchangeCode = "12345";
        AuthenticateUser query = AuthenticateUser.build(exchangeCode);

        User user = UserFixture.player()
                .id("QWEQWE")
                .build();

        DiscordUserDataResponse discordUserData = DiscordUserDataResponse.builder()
                .id(user.getDiscordId())
                .email("some@email.com")
                .globalNickname("someNickname")
                .username("someUsername")
                .build();

        AuthenticateUserResult authResult = AuthenticateUserResult.builder()
                .accessToken("token")
                .expiresIn(1234L)
                .refreshToken("token")
                .tokenType("type")
                .scope("scope")
                .build();

        when(discordAuthenticationPort.authenticate(any())).thenReturn(Mono.just(authResult));
        when(discordAuthenticationPort.retrieveLoggedUser(anyString())).thenReturn(Mono.just(discordUserData));
        when(repository.findByDiscordId(anyString())).thenReturn(Optional.of(user));

        // Then
        StepVerifier.create(handler.handle(query))
                .assertNext(result -> {
                    assertThat(result.getAccessToken()).isEqualTo(authResult.getAccessToken());
                    assertThat(result.getRefreshToken()).isEqualTo(authResult.getRefreshToken());
                    assertThat(result.getExpiresIn()).isEqualTo(authResult.getExpiresIn());
                    assertThat(result.getTokenType()).isEqualTo(authResult.getTokenType());
                    assertThat(result.getScope()).isEqualTo(authResult.getScope());
                })
                .verifyComplete();
    }

    @Test
    public void authenticateUser_whenUserNotExists_thenUserIsCreated_andThenUserIsAuthenticated() {

        // Given
        String exchangeCode = "12345";
        AuthenticateUser query = AuthenticateUser.build(exchangeCode);

        User user = UserFixture.player()
                .id("QWEQWE")
                .build();

        DiscordUserDataResponse discordUserData = DiscordUserDataResponse.builder()
                .id(user.getDiscordId())
                .email("some@email.com")
                .globalNickname("someNickname")
                .username("someUsername")
                .build();

        AuthenticateUserResult authResult = AuthenticateUserResult.builder()
                .accessToken("token")
                .expiresIn(1234L)
                .refreshToken("token")
                .tokenType("type")
                .scope("scope")
                .build();

        when(discordAuthenticationPort.authenticate(any())).thenReturn(Mono.just(authResult));
        when(discordAuthenticationPort.retrieveLoggedUser(anyString())).thenReturn(Mono.just(discordUserData));
        when(repository.findByDiscordId(anyString())).thenReturn(Optional.empty());
        when(repository.save(any())).thenReturn(user);

        // Then
        StepVerifier.create(handler.handle(query))
                .assertNext(result -> {
                    assertThat(result.getAccessToken()).isEqualTo(authResult.getAccessToken());
                    assertThat(result.getRefreshToken()).isEqualTo(authResult.getRefreshToken());
                    assertThat(result.getExpiresIn()).isEqualTo(authResult.getExpiresIn());
                    assertThat(result.getTokenType()).isEqualTo(authResult.getTokenType());
                    assertThat(result.getScope()).isEqualTo(authResult.getScope());
                })
                .verifyComplete();
    }
}
