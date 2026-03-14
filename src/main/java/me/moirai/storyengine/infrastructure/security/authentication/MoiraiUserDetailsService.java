package me.moirai.storyengine.infrastructure.security.authentication;

import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.exception.AuthenticationFailedException;
import me.moirai.storyengine.common.usecases.UseCaseRunner;
import me.moirai.storyengine.core.application.port.DiscordAuthenticationPort;
import me.moirai.storyengine.core.application.usecase.discord.userdetails.request.GetUserDetailsByDiscordId;
import me.moirai.storyengine.core.application.usecase.discord.userdetails.result.UserDetailsResult;
import me.moirai.storyengine.infrastructure.outbound.adapter.response.DiscordUserDataResponse;
import reactor.core.publisher.Mono;

@Service
public class MoiraiUserDetailsService implements ReactiveUserDetailsService {

    private final DiscordAuthenticationPort discordAuthenticationPort;
    private final UseCaseRunner useCaseRunner;

    public MoiraiUserDetailsService(
            DiscordAuthenticationPort discordAuthenticationPort,
            UseCaseRunner useCaseRunner) {

        this.discordAuthenticationPort = discordAuthenticationPort;
        this.useCaseRunner = useCaseRunner;
    }

    @Override
    public Mono<UserDetails> findByUsername(String tokenCluster) {

        String authorizationToken = tokenCluster.split(" / ")[0];
        String refreshToken = tokenCluster.split(" / ")[1];
        return discordAuthenticationPort.retrieveLoggedUser(authorizationToken)
                .map(userDetails -> getUserDetails(userDetails, authorizationToken, refreshToken));
    }

    private MoiraiPrincipal getUserDetails(
            DiscordUserDataResponse discordUser, String authorizationToken, String refreshToken) {

        try {
            GetUserDetailsByDiscordId query = GetUserDetailsByDiscordId.build(discordUser.getId());
            UserDetailsResult moiraiUser = useCaseRunner.run(query);

            return MoiraiPrincipal.builder()
                    .discordId(moiraiUser.getDiscordId())
                    .username(moiraiUser.getUsername())
                    .email(discordUser.getEmail())
                    .authorizationToken(authorizationToken)
                    .refreshToken(refreshToken)
                    .role(moiraiUser.getRole())
                    .build();
        } catch (AssetNotFoundException e) {
            throw new AuthenticationFailedException("Invalid user requested authentication", e);
        }
    }
}
