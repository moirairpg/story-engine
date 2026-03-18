package me.moirai.storyengine.core.port.inbound.discord.userdetails;

import me.moirai.storyengine.common.cqs.query.Query;

public record RefreshSessionToken(String refreshToken) implements Query<AuthenticateUserResult> {
}
