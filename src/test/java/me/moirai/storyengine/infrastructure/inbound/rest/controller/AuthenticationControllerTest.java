package me.moirai.storyengine.infrastructure.inbound.rest.controller;

import static me.moirai.storyengine.infrastructure.security.authentication.MoiraiCookie.SESSION_COOKIE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import me.moirai.storyengine.AbstractRestWebTest;
import me.moirai.storyengine.core.port.inbound.discord.userdetails.AuthenticateUser;
import me.moirai.storyengine.core.port.inbound.discord.userdetails.GetUserDetailsByDiscordId;
import me.moirai.storyengine.core.port.inbound.discord.userdetails.RefreshSessionToken;
import me.moirai.storyengine.core.port.inbound.discord.userdetails.AuthenticateUserResult;
import me.moirai.storyengine.core.port.inbound.discord.userdetails.UserDetailsResult;
import me.moirai.storyengine.core.port.DiscordAuthenticationPort;
import me.moirai.storyengine.infrastructure.inbound.rest.mapper.UserDataResponseMapper;
import me.moirai.storyengine.infrastructure.inbound.rest.response.UserDataResponse;
import me.moirai.storyengine.infrastructure.inbound.rest.response.UserDataResponseFixture;
import me.moirai.storyengine.infrastructure.security.authentication.config.AuthenticationSecurityConfig;
import reactor.core.publisher.Mono;

@WebFluxTest(properties = {
        "moirai.discord.oauth.client-id=clientId",
        "moirai.discord.oauth.client-secret=clientSecret",
        "moirai.discord.oauth.redirect-url=redirectUrl"
}, controllers = {
        AuthenticationController.class
}, excludeAutoConfiguration = {
        ReactiveSecurityAutoConfiguration.class,
        AuthenticationSecurityConfig.class
})
public class AuthenticationControllerTest extends AbstractRestWebTest {

    @MockBean
    protected DiscordAuthenticationPort discordAuthenticationPort;

    @MockBean
    private UserDataResponseMapper responseMapper;

    @Test
    public void exchangeCodeForToken() {

        // Given
        String code = "CODE";
        AuthenticateUserResult expectedResponse = AuthenticateUserResult.builder()
                .accessToken("TOKEN")
                .expiresIn(4324324L)
                .refreshToken("RFRSHTK")
                .scope("SCOPE")
                .build();

        when(useCaseRunner.run(any(AuthenticateUser.class))).thenReturn(Mono.just(expectedResponse));

        // Then
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/auth/code")
                        .queryParam("code", code)
                        .build())
                .exchange()
                .expectCookie().valueEquals(SESSION_COOKIE.getName(), "TOKEN");
    }

    @Test
    public void refreshToken() {

        // Given
        AuthenticateUserResult expectedResponse = AuthenticateUserResult.builder()
                .accessToken("NEW_TOKEN")
                .expiresIn(4324324L)
                .refreshToken("RFRSHTK")
                .scope("SCOPE")
                .build();

        when(useCaseRunner.run(any(RefreshSessionToken.class))).thenReturn(Mono.just(expectedResponse));

        // Then
        webTestClient.post()
                .uri("/auth/refresh")
                .exchange()
                .expectCookie().valueEquals(SESSION_COOKIE.getName(), "NEW_TOKEN");
    }

    @Test
    public void noTokenWhenExchangeCodeIsNull() {

        // Then
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/auth/code")
                        .build())
                .exchange()
                .expectCookie().doesNotExist(SESSION_COOKIE.getName());
    }

    @Test
    public void logout() {

        // Given
        Duration expiredCookie = Duration.ofSeconds(0);
        when(discordAuthenticationPort.logout(any(), any(), any(), any()))
                .thenReturn(Mono.empty());

        // Then
        webTestClient.post()
                .uri("/auth/logout")
                .exchange()
                .expectCookie().maxAge(SESSION_COOKIE.getName(), expiredCookie);
    }

    @Test
    public void getAuthenticatedUser() {

        // Given
        UserDataResponse result = UserDataResponseFixture.create().build();

        when(useCaseRunner.run(any(GetUserDetailsByDiscordId.class)))
                .thenReturn(mock(UserDetailsResult.class));

        when(responseMapper.toResponse(any(UserDetailsResult.class))).thenReturn(result);

        // Then
        webTestClient.get()
                .uri("/auth/user")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(UserDataResponse.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getDiscordId()).isEqualTo(result.getDiscordId());
                    assertThat(response.getNickname()).isEqualTo(result.getNickname());
                    assertThat(response.getUsername()).isEqualTo(result.getUsername());
                    assertThat(response.getAvatar()).isEqualTo(result.getAvatar());
                });

    }
}
