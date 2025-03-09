package me.moirai.discordbot.core.application.usecase.discord.userdetails.request;

import me.moirai.discordbot.common.usecases.UseCase;
import me.moirai.discordbot.core.application.usecase.discord.userdetails.result.AuthenticateUserResult;
import reactor.core.publisher.Mono;

public final class RefreshSessionToken extends UseCase<Mono<AuthenticateUserResult>> {

    private final String refreshToken;

    private RefreshSessionToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public static RefreshSessionToken build(String refreshToken) {
        return new RefreshSessionToken(refreshToken);
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
