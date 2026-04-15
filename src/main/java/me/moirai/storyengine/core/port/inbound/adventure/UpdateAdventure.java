package me.moirai.storyengine.core.port.inbound.adventure;

import java.util.List;
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
        ContextAttributesDto contextAttributes,
        List<LorebookEntryToAdd> lorebookEntriesToAdd,
        List<LorebookEntryToUpdate> lorebookEntriesToUpdate,
        List<UUID> lorebookEntriesToDelete)
        implements Command<AdventureDetails> {

    public record LorebookEntryToAdd(String name, String description, String playerId) {}

    public record LorebookEntryToUpdate(UUID id, String name, String description, String playerId) {}

    public UpdateAdventure {
        permissions = permissions != null ? Set.copyOf(permissions) : Set.of();
        lorebookEntriesToAdd = lorebookEntriesToAdd != null ? List.copyOf(lorebookEntriesToAdd) : List.of();
        lorebookEntriesToUpdate = lorebookEntriesToUpdate != null ? List.copyOf(lorebookEntriesToUpdate) : List.of();
        lorebookEntriesToDelete = lorebookEntriesToDelete != null ? List.copyOf(lorebookEntriesToDelete) : List.of();
    }
}
