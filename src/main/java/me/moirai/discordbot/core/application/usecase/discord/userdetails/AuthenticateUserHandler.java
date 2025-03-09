package me.moirai.discordbot.core.application.usecase.discord.userdetails;

import static io.micrometer.common.util.StringUtils.isBlank;
import static me.moirai.discordbot.core.domain.userdetails.Role.PLAYER;

import org.springframework.beans.factory.annotation.Value;

import me.moirai.discordbot.common.annotation.UseCaseHandler;
import me.moirai.discordbot.common.usecases.AbstractUseCaseHandler;
import me.moirai.discordbot.core.application.port.DiscordAuthenticationPort;
import me.moirai.discordbot.core.application.usecase.discord.userdetails.request.AuthenticateUser;
import me.moirai.discordbot.core.application.usecase.discord.userdetails.result.AuthenticateUserResult;
import me.moirai.discordbot.core.domain.userdetails.User;
import me.moirai.discordbot.core.domain.userdetails.UserDomainRepository;
import me.moirai.discordbot.infrastructure.inbound.rest.response.DiscordAuthResponse;
import me.moirai.discordbot.infrastructure.outbound.adapter.request.DiscordAuthRequest;
import me.moirai.discordbot.infrastructure.outbound.adapter.response.DiscordUserDataResponse;
import reactor.core.publisher.Mono;

@UseCaseHandler
public class AuthenticateUserHandler extends AbstractUseCaseHandler<AuthenticateUser, Mono<AuthenticateUserResult>> {

    private static final String DISCORD_SCOPE = "identify";
    private static final String DISCORD_GRANT_TYPE = "authorization_code";

    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final UserDomainRepository repository;
    private final DiscordAuthenticationPort discordAuthenticationPort;

    public AuthenticateUserHandler(
            @Value("${moirai.discord.oauth.client-id}") String clientId,
            @Value("${moirai.discord.oauth.client-secret}") String clientSecret,
            @Value("${moirai.discord.oauth.redirect-url}") String redirectUri,
            UserDomainRepository repository,
            DiscordAuthenticationPort discordAuthenticationPort) {

        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.repository = repository;
        this.discordAuthenticationPort = discordAuthenticationPort;
    }

    @Override
    public void validate(AuthenticateUser useCase) {

        if (isBlank(useCase.getAuthenticationCode())) {
            throw new IllegalArgumentException("Authentication code cannot be null");
        }
    }

    @Override
    public Mono<AuthenticateUserResult> execute(AuthenticateUser useCase) {

        DiscordAuthRequest request = createDiscordAuthRequest(useCase.getAuthenticationCode());
        return discordAuthenticationPort.authenticate(request)
                .flatMap(this::createUserIfNotExists)
                .map(this::toResult);
    }

    private DiscordAuthRequest createDiscordAuthRequest(String code) {

        return DiscordAuthRequest.builder()
                .code(code)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .redirectUri(redirectUri)
                .scope(DISCORD_SCOPE)
                .grantType(DISCORD_GRANT_TYPE)
                .build();
    }

    private Mono<DiscordAuthResponse> createUserIfNotExists(DiscordAuthResponse discordAuthResponse) {

        return discordAuthenticationPort.retrieveLoggedUser(discordAuthResponse.getAccessToken())
                .map(discordUserDetails -> {
                    repository.findByDiscordId(discordUserDetails.getId())
                            .orElseGet(() -> createUser(discordUserDetails));

                    return discordAuthResponse;
                });
    }

    private User createUser(DiscordUserDataResponse discordUserDetails) {

        return repository.save(User.builder()
                .discordId(discordUserDetails.getId())
                .creatorDiscordId(discordUserDetails.getId())
                .role(PLAYER)
                .build());
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
