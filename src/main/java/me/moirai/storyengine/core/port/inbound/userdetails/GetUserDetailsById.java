package me.moirai.storyengine.core.port.inbound.userdetails;

import java.util.UUID;

import me.moirai.storyengine.common.cqs.query.Query;

public record GetUserDetailsById(
        UUID userId,
        String discordToken)
        implements Query<UserDetailsResult> {
}
