package me.moirai.storyengine.infrastructure.inbound.rest.request;

import java.util.Set;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import me.moirai.storyengine.common.enums.Moderation;
import me.moirai.storyengine.common.enums.Visibility;
import me.moirai.storyengine.infrastructure.inbound.rest.validation.Moderated;

public record CreateAdventureRequest(
        @Moderated @NotEmpty(message = "cannot be empty") String name,
        @Moderated String description,
        UUID worldId,
        @Moderated String narratorName,
        @Moderated String narratorPersonality,
        @NotNull(message = "cannot be empty") Visibility visibility,
        @NotNull(message = "cannot be empty") Moderation moderation,
        boolean isMultiplayer,
        @Moderated @NotEmpty(message = "cannot be empty") String adventureStart,
        Set<AdventureLorebookEntryRequest> lorebook,
        Set<PermissionRequest> permissions,
        @NotNull(message = "cannot be null") @Valid ModelConfigurationRequest modelConfiguration,
        @Valid ContextAttributesRequest contextAttributes) {
}
