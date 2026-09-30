package me.moirai.storyengine.core.application.command.user;

import org.springframework.beans.factory.annotation.Value;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.core.port.inbound.userdetails.AuthenticateUser;
import me.moirai.storyengine.core.port.inbound.userdetails.AuthenticateUserResult;
import me.moirai.storyengine.core.port.outbound.discord.DiscordAuthRequest;
import me.moirai.storyengine.core.port.outbound.discord.DiscordAuthenticationPort;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

@CommandHandler
public class AuthenticateUserHandler extends AbstractCommandHandler<AuthenticateUser, AuthenticateUserResult> {

    private static final String DISCORD_SCOPE = "identify";
    private static final String DISCORD_GRANT_TYPE = "authorization_code";

    private final String clientId;
    private final String clientSecret;
    private final UserRepository repository;
    private final DiscordAuthenticationPort discordAuthenticationPort;

    public AuthenticateUserHandler(
            @Value("${moirai.discord.oauth.client-id}") String clientId,
            @Value("${moirai.discord.oauth.client-secret}") String clientSecret,
            UserRepository repository,
            DiscordAuthenticationPort discordAuthenticationPort) {

        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.repository = repository;
        this.discordAuthenticationPort = discordAuthenticationPort;
    }

    @Override
    public AuthenticateUserResult execute(AuthenticateUser useCase) {

        var request = createDiscordAuthRequest(useCase);
        var response = discordAuthenticationPort.authenticate(request);
        var discordUserDetails = discordAuthenticationPort.getLoggedUser(response.accessToken());
        var isRegistered = repository.findByDiscordId(discordUserDetails.id()).isPresent();

        return new AuthenticateUserResult(
                response.accessToken(),
                response.expiresIn(),
                response.refreshToken(),
                response.scope(),
                response.tokenType(),
                isRegistered);
    }

    private DiscordAuthRequest createDiscordAuthRequest(AuthenticateUser useCase) {

        return DiscordAuthRequest.builder()
                .code(useCase.authenticationCode())
                .clientId(clientId)
                .clientSecret(clientSecret)
                .redirectUri(useCase.redirectUri())
                .scope(DISCORD_SCOPE)
                .grantType(DISCORD_GRANT_TYPE)
                .build();
    }
}
