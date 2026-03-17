package me.moirai.storyengine.core.application.usecase.discord.userdetails;

import static io.micrometer.common.util.StringUtils.isBlank;

import org.springframework.beans.factory.annotation.Value;

import me.moirai.storyengine.common.annotation.QueryHandler;
import me.moirai.storyengine.common.cqs.query.AbstractQueryHandler;
import me.moirai.storyengine.core.port.inbound.discord.userdetails.AuthenticateUserResult;
import me.moirai.storyengine.core.port.inbound.discord.userdetails.RefreshSessionToken;
import me.moirai.storyengine.core.port.outbound.discord.DiscordAuthenticationPort;
import me.moirai.storyengine.core.port.outbound.discord.RefreshSessionTokenRequest;
import reactor.core.publisher.Mono;

@QueryHandler
public class RefreshSessionTokenHandler
        extends AbstractQueryHandler<RefreshSessionToken, Mono<AuthenticateUserResult>> {

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

        if (isBlank(useCase.refreshToken())) {
            throw new IllegalArgumentException("Refresh token cannot be null");
        }
    }

    @Override
    public Mono<AuthenticateUserResult> execute(RefreshSessionToken useCase) {

        var request = createDiscordAuthRequest(useCase.refreshToken());
        return discordAuthenticationPort.refreshSessionToken(request);
    }

    private RefreshSessionTokenRequest createDiscordAuthRequest(String refreshToken) {

        return RefreshSessionTokenRequest.builder()
                .refreshToken(refreshToken)
                .grantType(DISCORD_GRANT_TYPE)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build();
    }
}
