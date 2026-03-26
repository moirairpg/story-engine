package me.moirai.storyengine.core.port.inbound.userdetails;

import me.moirai.storyengine.common.cqs.query.Query;

public record RefreshSessionToken(String refreshToken) implements Query<AuthenticateUserResult> {
}
