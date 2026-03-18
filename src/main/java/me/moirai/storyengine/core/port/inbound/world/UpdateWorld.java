package me.moirai.storyengine.core.port.inbound.world;

import java.util.Set;
import java.util.UUID;

import me.moirai.storyengine.common.cqs.command.Command;
import me.moirai.storyengine.common.enums.Visibility;

public record UpdateWorld(
        UUID worldId,
        String name,
        String description,
        String adventureStart,
        Visibility visibility,
        String requesterId,
        Set<String> usersAllowedToWriteToAdd,
        Set<String> usersAllowedToWriteToRemove,
        Set<String> usersAllowedToReadToAdd,
        Set<String> usersAllowedToReadToRemove)
        implements Command<WorldDetails> {
}