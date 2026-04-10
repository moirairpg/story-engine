package me.moirai.storyengine.core.application.query.user;

import static io.micrometer.common.util.StringUtils.isBlank;
import static me.moirai.storyengine.common.enums.Role.PLAYER;

import org.springframework.beans.factory.annotation.Value;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.core.domain.userdetails.User;
import me.moirai.storyengine.core.port.inbound.userdetails.AuthenticateUser;
import me.moirai.storyengine.core.port.inbound.userdetails.AuthenticateUserResult;
import me.moirai.storyengine.core.port.outbound.discord.DiscordAuthRequest;
import me.moirai.storyengine.core.port.outbound.discord.DiscordAuthenticationPort;
import me.moirai.storyengine.core.port.outbound.discord.DiscordUserDataResponse;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

@CommandHandler
public class AuthenticateUserHandler extends AbstractCommandHandler<AuthenticateUser, AuthenticateUserResult> {

    private static final String DISCORD_SCOPE = "identify";
    private static final String DISCORD_GRANT_TYPE = "authorization_code";

    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final UserRepository repository;
    private final DiscordAuthenticationPort discordAuthenticationPort;

    public AuthenticateUserHandler(
            @Value("${moirai.discord.oauth.client-id}") String clientId,
            @Value("${moirai.discord.oauth.client-secret}") String clientSecret,
            @Value("${moirai.discord.oauth.redirect-url}") String redirectUri,
            UserRepository repository,
            DiscordAuthenticationPort discordAuthenticationPort) {

        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.repository = repository;
        this.discordAuthenticationPort = discordAuthenticationPort;
    }

    @Override
    public void validate(AuthenticateUser useCase) {

        if (isBlank(useCase.authenticationCode())) {
            throw new IllegalArgumentException("Authentication code cannot be null");
        }
    }

    @Override
    public AuthenticateUserResult execute(AuthenticateUser useCase) {

        var request = createDiscordAuthRequest(useCase);
        var response = discordAuthenticationPort.authenticate(request);

        return createUserIfNotExists(response);
    }

    private DiscordAuthRequest createDiscordAuthRequest(AuthenticateUser useCase) {

        return DiscordAuthRequest.builder()
                .code(useCase.authenticationCode())
                .clientId(clientId)
                .clientSecret(clientSecret)
                .redirectUri(redirectUri)
                .scope(DISCORD_SCOPE)
                .grantType(DISCORD_GRANT_TYPE)
                .build();
    }

    private AuthenticateUserResult createUserIfNotExists(AuthenticateUserResult authenticateUserResult) {

        var discordUserDetails = discordAuthenticationPort.getLoggedUser(authenticateUserResult.accessToken());

        repository.findByDiscordId(discordUserDetails.id())
                .orElseGet(() -> createUser(discordUserDetails));

        return authenticateUserResult;
    }

    private User createUser(DiscordUserDataResponse discordUserDetails) {

        return repository.save(User.builder()
                .discordId(discordUserDetails.id())
                .username(discordUserDetails.username())
                .role(PLAYER)
                .build());
    }
}
