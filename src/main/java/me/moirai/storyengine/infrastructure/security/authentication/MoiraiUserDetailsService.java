package me.moirai.storyengine.infrastructure.security.authentication;

import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import me.moirai.storyengine.common.cqs.query.QueryRunner;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.exception.AuthenticationFailedException;
import me.moirai.storyengine.core.port.inbound.discord.userdetails.GetUserDetailsByDiscordId;
import me.moirai.storyengine.core.port.outbound.discord.DiscordAuthenticationPort;
import me.moirai.storyengine.core.port.outbound.discord.DiscordUserDataResponse;
import reactor.core.publisher.Mono;

@Service
public class MoiraiUserDetailsService implements ReactiveUserDetailsService {

    private final DiscordAuthenticationPort discordAuthenticationPort;
    private final QueryRunner queryRunner;

    public MoiraiUserDetailsService(
            DiscordAuthenticationPort discordAuthenticationPort,
            QueryRunner queryRunner) {

        this.discordAuthenticationPort = discordAuthenticationPort;
        this.queryRunner = queryRunner;
    }

    @Override
    public Mono<UserDetails> findByUsername(String tokenCluster) {

        var authorizationToken = tokenCluster.split(" / ")[0];
        var refreshToken = tokenCluster.split(" / ")[1];
        return discordAuthenticationPort.retrieveLoggedUser(authorizationToken)
                .map(userDetails -> getUserDetails(userDetails, authorizationToken, refreshToken));
    }

    private MoiraiPrincipal getUserDetails(
            DiscordUserDataResponse discordUser,
            String authorizationToken,
            String refreshToken) {

        try {
            var query = new GetUserDetailsByDiscordId(discordUser.id());
            var moiraiUser = queryRunner.run(query);

            return new MoiraiPrincipal(
                    moiraiUser.publicId(),
                    moiraiUser.discordId(),
                    moiraiUser.username(),
                    discordUser.email(),
                    authorizationToken,
                    refreshToken,
                    moiraiUser.role(),
                    null);
        } catch (AssetNotFoundException e) {
            throw new AuthenticationFailedException("Invalid user requested authentication", e);
        }
    }
}
