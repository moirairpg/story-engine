package me.moirai.storyengine.infrastructure.inbound.rest.request;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import me.moirai.storyengine.common.enums.Visibility;
import me.moirai.storyengine.infrastructure.inbound.rest.validation.Moderated;
import me.moirai.storyengine.infrastructure.inbound.rest.validation.ModeratedLorebook;

public record UpdateWorldRequest(
        @Moderated @NotEmpty(message = "cannot be empty") String name,
        @Moderated @NotEmpty(message = "cannot be empty") String description,
        @Moderated @NotEmpty(message = "cannot be empty") String adventureStart,
        @Moderated String narratorName,
        @Moderated String narratorPersonality,
        @NotNull(message = "cannot be null") Visibility visibility,
        Set<PermissionRequest> permissions,
        @ModeratedLorebook List<WorldLorebookEntryRequest> lorebookEntriesToAdd,
        @ModeratedLorebook List<UpdateWorldLorebookEntryRequest> lorebookEntriesToUpdate,
        List<UUID> lorebookEntriesToDelete) {
}
