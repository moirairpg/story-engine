package me.moirai.storyengine.core.application.port;

import me.moirai.storyengine.infrastructure.inbound.rest.response.DiscordAuthResponse;
import me.moirai.storyengine.infrastructure.outbound.adapter.request.DiscordAuthRequest;
import me.moirai.storyengine.infrastructure.outbound.adapter.request.DiscordTokenRevocationRequest;
import me.moirai.storyengine.infrastructure.outbound.adapter.request.RefreshSessionTokenRequest;
import me.moirai.storyengine.infrastructure.outbound.adapter.response.DiscordUserDataResponse;
import reactor.core.publisher.Mono;

public interface DiscordAuthenticationPort {

    Mono<DiscordAuthResponse> authenticate(DiscordAuthRequest request);

    Mono<DiscordAuthResponse> refreshSessionToken(RefreshSessionTokenRequest request);

    Mono<DiscordUserDataResponse> retrieveLoggedUser(String token);

    Mono<Void> logout(DiscordTokenRevocationRequest request);
}
