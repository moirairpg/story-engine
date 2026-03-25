package me.moirai.storyengine.core.port.inbound.discord.userdetails;

import java.util.UUID;

import me.moirai.storyengine.common.cqs.query.Query;

public record GetUserDetailsById(UUID userId) implements Query<UserDetailsResult> {
}
