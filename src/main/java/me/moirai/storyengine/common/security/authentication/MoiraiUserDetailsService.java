package me.moirai.storyengine.common.security.authentication;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.common.exception.AuthenticationFailedException;
import me.moirai.storyengine.core.port.outbound.discord.DiscordAuthenticationPort;
import me.moirai.storyengine.core.port.outbound.discord.DiscordUserDataResponse;
import me.moirai.storyengine.core.port.outbound.userdetails.UserReader;

@Service
public class MoiraiUserDetailsService implements UserDetailsService {

    private final DiscordAuthenticationPort discordAuthenticationPort;
    private final UserReader userReader;

    public MoiraiUserDetailsService(
            DiscordAuthenticationPort discordAuthenticationPort,
            UserReader userReader) {

        this.discordAuthenticationPort = discordAuthenticationPort;
        this.userReader = userReader;
    }

    @Override
    public UserDetails loadUserByUsername(String tokenCluster) throws UsernameNotFoundException {

        var authorizationToken = tokenCluster.split(" / ")[0];
        var refreshToken = tokenCluster.split(" / ")[1];
        var loggedUser = discordAuthenticationPort.getLoggedUser(authorizationToken);

        return getUserDetails(loggedUser, authorizationToken, refreshToken);
    }

    private MoiraiPrincipal getUserDetails(
            DiscordUserDataResponse discordUser,
            String authorizationToken,
            String refreshToken) {

        try {
            var moiraiUser = userReader.getUserByDiscordId(discordUser.id())
                    .orElseThrow(() -> new NotFoundException("User not found"));

            return new MoiraiPrincipal(
                    moiraiUser.publicId(),
                    moiraiUser.id(),
                    moiraiUser.discordId(),
                    discordUser.username(),
                    discordUser.email(),
                    authorizationToken,
                    refreshToken,
                    moiraiUser.role(),
                    null);
        } catch (NotFoundException e) {
            throw new AuthenticationFailedException("Invalid user requested authentication", e);
        }
    }
}
