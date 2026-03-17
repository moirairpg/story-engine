package me.moirai.storyengine.core.port.inbound.discord.userdetails;

import me.moirai.storyengine.common.cqs.query.Query;
import reactor.core.publisher.Mono;

public record RefreshSessionToken(String refreshToken) implements Query<Mono<AuthenticateUserResult>> {
}
