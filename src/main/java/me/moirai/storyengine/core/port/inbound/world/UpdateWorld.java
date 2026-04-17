package me.moirai.storyengine.core.port.inbound.world;

import java.util.List;
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
        String narratorName,
        String narratorPersonality,
        Visibility visibility,
        Double uiImagePositionX,
        Double uiImagePositionY,
        Set<PermissionDto> permissions,
        List<LorebookEntryToAdd> lorebookEntriesToAdd,
        List<LorebookEntryToUpdate> lorebookEntriesToUpdate,
        List<UUID> lorebookEntriesToDelete)
        implements Command<WorldDetails> {

    public record LorebookEntryToAdd(String name, String description) {}

    public record LorebookEntryToUpdate(UUID id, String name, String description) {}

    public UpdateWorld {
        permissions = permissions != null ? Set.copyOf(permissions) : Set.of();
        lorebookEntriesToAdd = lorebookEntriesToAdd != null ? List.copyOf(lorebookEntriesToAdd) : List.of();
        lorebookEntriesToUpdate = lorebookEntriesToUpdate != null ? List.copyOf(lorebookEntriesToUpdate) : List.of();
        lorebookEntriesToDelete = lorebookEntriesToDelete != null ? List.copyOf(lorebookEntriesToDelete) : List.of();
    }
}
