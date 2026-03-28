package me.moirai.storyengine.core.port.inbound.adventure;

import java.util.Set;
import java.util.UUID;

import me.moirai.storyengine.common.cqs.command.Command;
import me.moirai.storyengine.common.dto.PermissionDto;
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
        Set<PermissionDto> permissions,
        ModelConfigurationDto modelConfiguration,
        ContextAttributesDto contextAttributes)
        implements Command<AdventureDetails> {

    public CreateAdventure {
        permissions = permissions != null ? Set.copyOf(permissions) : Set.of();
    }
}
