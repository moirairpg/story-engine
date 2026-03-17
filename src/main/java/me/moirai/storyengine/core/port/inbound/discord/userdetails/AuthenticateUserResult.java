package me.moirai.storyengine.core.port.inbound.discord.userdetails;

public record AuthenticateUserResult(
        String accessToken,
        Long expiresIn,
        String refreshToken,
        String scope,
        String tokenType) {
}
