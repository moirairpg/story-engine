package me.moirai.storyengine.core.application.usecase.discord.userdetails;

import static io.micrometer.common.util.StringUtils.isBlank;

import org.springframework.beans.factory.annotation.Value;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.application.usecase.discord.userdetails.request.RefreshSessionToken;
import me.moirai.storyengine.core.application.usecase.discord.userdetails.result.AuthenticateUserResult;
import me.moirai.storyengine.core.port.DiscordAuthenticationPort;
import me.moirai.storyengine.infrastructure.inbound.rest.response.DiscordAuthResponse;
import me.moirai.storyengine.infrastructure.outbound.adapter.request.RefreshSessionTokenRequest;
import reactor.core.publisher.Mono;

@UseCaseHandler
public class RefreshSessionTokenHandler
        extends AbstractUseCaseHandler<RefreshSessionToken, Mono<AuthenticateUserResult>> {

    private static final String DISCORD_GRANT_TYPE = "refresh_token";

    private final String clientId;
    private final String clientSecret;
    private final DiscordAuthenticationPort discordAuthenticationPort;

    public RefreshSessionTokenHandler(
            @Value("${moirai.discord.oauth.client-id}") String clientId,
            @Value("${moirai.discord.oauth.client-secret}") String clientSecret,
            DiscordAuthenticationPort discordAuthenticationPort) {

        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.discordAuthenticationPort = discordAuthenticationPort;
    }

    @Override
    public void validate(RefreshSessionToken useCase) {

        if (isBlank(useCase.getRefreshToken())) {
            throw new IllegalArgumentException("Refresh token cannot be null");
        }
    }

    @Override
    public Mono<AuthenticateUserResult> execute(RefreshSessionToken useCase) {

        RefreshSessionTokenRequest request = createDiscordAuthRequest(useCase.getRefreshToken());
        return discordAuthenticationPort.refreshSessionToken(request)
                .map(this::toResult);
    }

    private RefreshSessionTokenRequest createDiscordAuthRequest(String refreshToken) {

        return RefreshSessionTokenRequest.builder()
                .refreshToken(refreshToken)
                .grantType(DISCORD_GRANT_TYPE)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build();
    }

    private AuthenticateUserResult toResult(DiscordAuthResponse discordAuthResponse) {

        return AuthenticateUserResult.builder()
                .accessToken(discordAuthResponse.getAccessToken())
                .refreshToken(discordAuthResponse.getRefreshToken())
                .expiresIn(discordAuthResponse.getExpiresIn())
                .tokenType(discordAuthResponse.getTokenType())
                .scope(discordAuthResponse.getScope())
                .build();
    }
}
