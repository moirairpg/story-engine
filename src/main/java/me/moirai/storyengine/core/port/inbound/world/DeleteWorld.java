package me.moirai.storyengine.core.port.inbound.world;

import java.util.UUID;

import me.moirai.storyengine.common.cqs.command.Command;

public record DeleteWorld(
        UUID worldId,
        String requesterId)
        implements Command<Void> {
}
