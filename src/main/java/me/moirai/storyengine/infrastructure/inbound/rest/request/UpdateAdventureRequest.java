package me.moirai.storyengine.infrastructure.inbound.rest.request;

import java.util.Set;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import me.moirai.storyengine.common.enums.Moderation;
import me.moirai.storyengine.common.enums.Visibility;
import me.moirai.storyengine.infrastructure.inbound.rest.validation.Moderated;

public record UpdateAdventureRequest(
        @Moderated @NotEmpty(message = "cannot be empty") String name,
        @Moderated String description,
        @Moderated String narratorName,
        @Moderated String narratorPersonality,
        @NotNull(message = "cannot be empty") Visibility visibility,
        @NotNull(message = "cannot be empty") Moderation moderation,
        boolean isMultiplayer,
        Set<PermissionRequest> permissions,
        @Moderated String adventureStart,
        @NotNull(message = "cannot be null") @Valid ModelConfigurationRequest modelConfiguration,
        @Valid ContextAttributesRequest contextAttributes) {
}
