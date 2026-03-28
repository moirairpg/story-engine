package me.moirai.storyengine.core.port.inbound.adventure;

import java.util.Set;
import java.util.UUID;

import me.moirai.storyengine.common.cqs.command.Command;
import me.moirai.storyengine.common.enums.Moderation;
import me.moirai.storyengine.common.enums.Visibility;

public record UpdateAdventure(
        UUID adventureId,
        String description,
        String adventureStart,
        String name,
        UUID worldId,
        UUID personaId,
        Visibility visibility,
        Moderation moderation,
        boolean isMultiplayer,
        Set<Long> usersAllowedToWriteToAdd,
        Set<Long> usersAllowedToWriteToRemove,
        Set<Long> usersAllowedToReadToAdd,
        Set<Long> usersAllowedToReadToRemove,
        UpdateModelConfigurationDto modelConfiguration,
        ContextAttributesDto contextAttributes)
        implements Command<AdventureDetails> {
}
