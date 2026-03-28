package me.moirai.storyengine.core.port.inbound.adventure;

import java.util.Set;
import java.util.UUID;

import me.moirai.storyengine.common.cqs.command.Command;
import me.moirai.storyengine.common.enums.Moderation;
import me.moirai.storyengine.common.enums.Visibility;

public record CreateAdventure(
        String name,
        String description,
        UUID worldId,
        UUID personaId,
        Visibility visibility,
        Moderation moderation,
        boolean isMultiplayer,
        Set<Long> usersAllowedToWrite,
        Set<Long> usersAllowedToRead,
        ModelConfigurationDto modelConfiguration,
        ContextAttributesDto contextAttributes)
        implements Command<AdventureDetails> {
}
