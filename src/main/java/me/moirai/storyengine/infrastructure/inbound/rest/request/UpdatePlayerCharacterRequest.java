package me.moirai.storyengine.infrastructure.inbound.rest.request;

import jakarta.validation.constraints.NotBlank;
import me.moirai.storyengine.infrastructure.inbound.rest.validation.Moderated;

public record UpdatePlayerCharacterRequest(
        @NotBlank(message = "cannot be null") @Moderated String name,
        @NotBlank(message = "cannot be null") @Moderated String personality,
        @NotBlank(message = "cannot be null") @Moderated String physicalDescription,
        @NotBlank(message = "cannot be null") @Moderated String background,
        Double uiImagePositionX,
        Double uiImagePositionY) {
}
