package me.moirai.storyengine.core.application.usecase.discord.userdetails.request;

import me.moirai.storyengine.common.usecases.UseCase;
import me.moirai.storyengine.core.application.usecase.discord.userdetails.result.AuthenticateUserResult;
import reactor.core.publisher.Mono;

public final class AuthenticateUser extends UseCase<Mono<AuthenticateUserResult>> {

    private final String authenticationCode;

    private AuthenticateUser(String authenticationCode) {
        this.authenticationCode = authenticationCode;
    }

    public static AuthenticateUser build(String authenticationCode) {
        return new AuthenticateUser(authenticationCode);
    }

    public String getAuthenticationCode() {
        return authenticationCode;
    }
}
