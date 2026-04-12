package me.moirai.storyengine.core.port.inbound.adventure;

import java.util.Set;
import java.util.UUID;

import me.moirai.storyengine.common.cqs.command.Command;
import me.moirai.storyengine.common.dto.PermissionDto;
import me.moirai.storyengine.common.enums.Moderation;
import me.moirai.storyengine.common.enums.Visibility;

public record UpdateAdventure(
        UUID adventureId,
        String name,
        String description,
        String adventureStart,
        String narratorName,
        String narratorPersonality,
        Visibility visibility,
        Moderation moderation,
        boolean isMultiplayer,
        Set<PermissionDto> permissions,
        ModelConfigurationDto modelConfiguration,
        ContextAttributesDto contextAttributes)
        implements Command<AdventureDetails> {

    public UpdateAdventure {
        permissions = permissions != null ? Set.copyOf(permissions) : Set.of();
    }
}
