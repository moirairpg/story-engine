package me.moirai.storyengine.core.port.inbound.discord.userdetails;

import me.moirai.storyengine.common.usecases.UseCase;
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
