package me.moirai.storyengine.infrastructure.inbound.rest.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import me.moirai.storyengine.common.enums.CharacterClass;
import me.moirai.storyengine.infrastructure.inbound.rest.validation.Moderated;

public record UpdatePlayerCharacterRequest(
        @NotEmpty(message = "cannot be null") @Moderated String name,
        @NotNull(message = "cannot be null") CharacterClass characterClass,
        @NotEmpty(message = "cannot be null") @Moderated String personality,
        @NotEmpty(message = "cannot be null") @Moderated String physicalDescription) {
}