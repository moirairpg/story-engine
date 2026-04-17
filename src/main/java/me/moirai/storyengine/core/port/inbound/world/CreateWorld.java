package me.moirai.storyengine.core.port.inbound.world;

import java.util.List;
import java.util.Set;

import me.moirai.storyengine.common.cqs.command.Command;
import me.moirai.storyengine.common.dto.PermissionDto;
import me.moirai.storyengine.common.enums.Visibility;

public record CreateWorld(
        String name,
        String description,
        String adventureStart,
        String narratorName,
        String narratorPersonality,
        Visibility visibility,
        Double uiImagePositionX,
        Double uiImagePositionY,
        List<LorebookEntry> lorebookEntries,
        Set<PermissionDto> permissions)
        implements Command<WorldDetails> {

    public CreateWorld {
        permissions = permissions != null ? Set.copyOf(permissions) : Set.of();
    }

    public record LorebookEntry(
            String name,
            String description) {
    }
}
