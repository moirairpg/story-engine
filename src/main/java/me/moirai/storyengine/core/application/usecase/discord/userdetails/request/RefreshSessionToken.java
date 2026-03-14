package me.moirai.storyengine.core.application.usecase.discord.userdetails.request;

import me.moirai.storyengine.common.usecases.UseCase;
import me.moirai.storyengine.core.application.usecase.discord.userdetails.result.AuthenticateUserResult;
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
