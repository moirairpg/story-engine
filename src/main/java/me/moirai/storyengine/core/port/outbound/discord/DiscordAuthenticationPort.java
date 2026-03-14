package me.moirai.storyengine.core.port.outbound.discord;

import me.moirai.storyengine.core.port.inbound.discord.userdetails.AuthenticateUserResult;
import reactor.core.publisher.Mono;

public interface DiscordAuthenticationPort {

    Mono<AuthenticateUserResult> authenticate(DiscordAuthRequest request);

    Mono<AuthenticateUserResult> refreshSessionToken(RefreshSessionTokenRequest request);

    Mono<DiscordUserDataResponse> retrieveLoggedUser(String token);

    Mono<Void> logout(String clientId, String clientSecret, String token, String tokenTypeHint);
}
