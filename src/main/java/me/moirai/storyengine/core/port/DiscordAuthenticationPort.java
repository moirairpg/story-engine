package me.moirai.storyengine.core.port;

import me.moirai.storyengine.core.port.outbound.DiscordAuthRequest;
import me.moirai.storyengine.core.port.outbound.DiscordUserDataResponse;
import me.moirai.storyengine.core.port.outbound.RefreshSessionTokenRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.response.DiscordAuthResponse;
import reactor.core.publisher.Mono;

public interface DiscordAuthenticationPort {

    Mono<DiscordAuthResponse> authenticate(DiscordAuthRequest request);

    Mono<DiscordAuthResponse> refreshSessionToken(RefreshSessionTokenRequest request);

    Mono<DiscordUserDataResponse> retrieveLoggedUser(String token);

    Mono<Void> logout(String clientId, String clientSecret, String token, String tokenTypeHint);
}
