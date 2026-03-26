package me.moirai.storyengine.core.application.query.user;

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

import me.moirai.storyengine.core.domain.userdetails.UserFixture;
import me.moirai.storyengine.core.port.inbound.userdetails.AuthenticateUser;
import me.moirai.storyengine.core.port.inbound.userdetails.AuthenticateUserResult;
import me.moirai.storyengine.core.port.outbound.discord.DiscordAuthenticationPort;
import me.moirai.storyengine.core.port.outbound.discord.DiscordUserDataResponse;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

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
        String exchangeCode = null;
        var expectedMessage = "Authentication code cannot be null";
        var query = new AuthenticateUser(exchangeCode);

        // Then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> handler.handle(query))
                .withMessage(expectedMessage);
    }

    @Test
    public void authenticateUser_whenDataIsValid_thenUserIsAuthenticated() {

        // Given
        var exchangeCode = "12345";
        var query = new AuthenticateUser(exchangeCode);

        var user = UserFixture.player().build();

        var discordUserData = new DiscordUserDataResponse(
                user.getDiscordId(),
                "someUsername",
                "someNickname",
                null,
                "some@email.com",
                null);

        var authResult = new AuthenticateUserResult(
                "token",
                1234L,
                "token",
                "scope",
                "type");

        when(discordAuthenticationPort.authenticate(any())).thenReturn(authResult);
        when(discordAuthenticationPort.retrieveLoggedUser(anyString())).thenReturn(discordUserData);
        when(repository.findByDiscordId(anyString())).thenReturn(Optional.of(user));

        // When
        var result = handler.handle(query);

        // Then
        assertThat(result.accessToken()).isEqualTo(authResult.accessToken());
        assertThat(result.refreshToken()).isEqualTo(authResult.refreshToken());
        assertThat(result.expiresIn()).isEqualTo(authResult.expiresIn());
        assertThat(result.tokenType()).isEqualTo(authResult.tokenType());
        assertThat(result.scope()).isEqualTo(authResult.scope());
    }

    @Test
    public void authenticateUser_whenUserNotExists_thenUserIsCreated_andThenUserIsAuthenticated() {

        // Given
        var exchangeCode = "12345";
        var query = new AuthenticateUser(exchangeCode);

        var user = UserFixture.player().build();

        var discordUserData = new DiscordUserDataResponse(
                user.getDiscordId(),
                "someUsername",
                "someNickname",
                null,
                "some@email.com",
                null);

        var authResult = new AuthenticateUserResult(
                "token",
                1234L,
                "token",
                "scope",
                "type");

        when(discordAuthenticationPort.authenticate(any())).thenReturn(authResult);
        when(discordAuthenticationPort.retrieveLoggedUser(anyString())).thenReturn(discordUserData);
        when(repository.findByDiscordId(anyString())).thenReturn(Optional.empty());
        when(repository.save(any())).thenReturn(user);

        // When
        var result = handler.handle(query);

        // Then
        assertThat(result.accessToken()).isEqualTo(authResult.accessToken());
        assertThat(result.refreshToken()).isEqualTo(authResult.refreshToken());
        assertThat(result.expiresIn()).isEqualTo(authResult.expiresIn());
        assertThat(result.tokenType()).isEqualTo(authResult.tokenType());
        assertThat(result.scope()).isEqualTo(authResult.scope());
    }
}
