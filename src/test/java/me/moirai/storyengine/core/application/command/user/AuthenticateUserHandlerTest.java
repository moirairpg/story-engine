package me.moirai.storyengine.core.application.command.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.core.domain.userdetails.UserFixture;
import me.moirai.storyengine.core.port.inbound.userdetails.AuthenticateUser;
import me.moirai.storyengine.core.port.inbound.userdetails.AuthenticateUserResult;
import me.moirai.storyengine.core.port.outbound.discord.DiscordAuthRequest;
import me.moirai.storyengine.core.port.outbound.discord.DiscordAuthenticationPort;
import me.moirai.storyengine.core.port.outbound.discord.DiscordUserDataResponse;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

@ExtendWith(MockitoExtension.class)
public class AuthenticateUserHandlerTest {

    private static final String REDIRECT_URI = "http://localhost/auth/signin/code";

    @Mock
    private UserRepository repository;

    @Mock
    private DiscordAuthenticationPort discordAuthenticationPort;

    private AuthenticateUserHandler handler;

    @BeforeEach
    public void before() {

        handler = new AuthenticateUserHandler(
                "/someuri",
                "/someuri",
                repository,
                discordAuthenticationPort);
    }

    @Test
    public void authenticateUser_whenDataIsValid_thenUserIsAuthenticated() {

        // Given
        var exchangeCode = "12345";
        var query = new AuthenticateUser(exchangeCode, REDIRECT_URI);

        var user = UserFixture.player().build();
        var discordUserData = discordUserData(user.getDiscordId());
        var authResult = authResult();

        when(discordAuthenticationPort.authenticate(any())).thenReturn(authResult);
        when(discordAuthenticationPort.getLoggedUser(anyString())).thenReturn(discordUserData);
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
    public void shouldReportRegisteredWhenDiscordAccountHasAMoiraiAccount() {

        // Given
        var query = new AuthenticateUser("12345", REDIRECT_URI);
        var user = UserFixture.player().build();

        when(discordAuthenticationPort.authenticate(any())).thenReturn(authResult());
        when(discordAuthenticationPort.getLoggedUser(anyString())).thenReturn(discordUserData(user.getDiscordId()));
        when(repository.findByDiscordId(anyString())).thenReturn(Optional.of(user));

        // When
        var result = handler.handle(query);

        // Then
        assertThat(result.isRegistered()).isTrue();
    }

    @Test
    public void shouldReportNotRegisteredWhenDiscordAccountHasNoMoiraiAccount() {

        // Given
        var query = new AuthenticateUser("12345", REDIRECT_URI);

        when(discordAuthenticationPort.authenticate(any())).thenReturn(authResult());
        when(discordAuthenticationPort.getLoggedUser(anyString())).thenReturn(discordUserData("99999"));
        when(repository.findByDiscordId(anyString())).thenReturn(Optional.empty());

        // When
        var result = handler.handle(query);

        // Then
        assertThat(result.isRegistered()).isFalse();
    }

    @Test
    public void shouldNeverCreateAUserWhenDiscordAccountHasNoMoiraiAccount() {

        // Given
        var query = new AuthenticateUser("12345", REDIRECT_URI);

        when(discordAuthenticationPort.authenticate(any())).thenReturn(authResult());
        when(discordAuthenticationPort.getLoggedUser(anyString())).thenReturn(discordUserData("99999"));
        when(repository.findByDiscordId(anyString())).thenReturn(Optional.empty());

        // When
        handler.handle(query);

        // Then
        verify(repository, never()).save(any());
    }

    @Test
    public void shouldSendTheCommandRedirectUriToDiscordWhenExchangingTheCode() {

        // Given
        var query = new AuthenticateUser("12345", REDIRECT_URI);
        var requestCaptor = ArgumentCaptor.forClass(DiscordAuthRequest.class);

        when(discordAuthenticationPort.authenticate(requestCaptor.capture())).thenReturn(authResult());
        when(discordAuthenticationPort.getLoggedUser(anyString())).thenReturn(discordUserData("99999"));
        when(repository.findByDiscordId(anyString())).thenReturn(Optional.empty());

        // When
        handler.handle(query);

        // Then
        assertThat(requestCaptor.getValue().getRedirectUri()).isEqualTo(REDIRECT_URI);
    }

    private DiscordUserDataResponse discordUserData(String discordId) {

        return new DiscordUserDataResponse(
                discordId,
                "someUsername",
                null,
                null,
                "some@email.com",
                null,
                null);
    }

    private AuthenticateUserResult authResult() {

        return new AuthenticateUserResult(
                "token",
                1234L,
                "token",
                "scope",
                "type",
                false);
    }
}
