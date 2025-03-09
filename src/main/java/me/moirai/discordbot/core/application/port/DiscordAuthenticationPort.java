package me.moirai.discordbot.core.application.port;

import me.moirai.discordbot.infrastructure.inbound.rest.response.DiscordAuthResponse;
import me.moirai.discordbot.infrastructure.outbound.adapter.request.DiscordAuthRequest;
import me.moirai.discordbot.infrastructure.outbound.adapter.request.DiscordTokenRevocationRequest;
import me.moirai.discordbot.infrastructure.outbound.adapter.request.RefreshSessionTokenRequest;
import me.moirai.discordbot.infrastructure.outbound.adapter.response.DiscordUserDataResponse;
import reactor.core.publisher.Mono;

public interface DiscordAuthenticationPort {

    Mono<DiscordAuthResponse> authenticate(DiscordAuthRequest request);

    Mono<DiscordAuthResponse> refreshSessionToken(RefreshSessionTokenRequest request);

    Mono<DiscordUserDataResponse> retrieveLoggedUser(String token);

    Mono<Void> logout(DiscordTokenRevocationRequest request);
}
