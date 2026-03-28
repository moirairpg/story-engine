package me.moirai.storyengine.core.port.inbound.world;

import java.util.Set;
import java.util.UUID;

import me.moirai.storyengine.common.cqs.command.Command;
import me.moirai.storyengine.common.dto.PermissionDto;
import me.moirai.storyengine.common.enums.Visibility;

public record UpdateWorld(
        UUID worldId,
        String name,
        String description,
        String adventureStart,
        Visibility visibility,
        Set<PermissionDto> permissions)
        implements Command<WorldDetails> {

    public UpdateWorld {
        permissions = permissions != null ? Set.copyOf(permissions) : Set.of();
    }
}
