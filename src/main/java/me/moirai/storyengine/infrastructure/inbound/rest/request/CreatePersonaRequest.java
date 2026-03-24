package me.moirai.storyengine.infrastructure.inbound.rest.request;

import java.util.Set;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import me.moirai.storyengine.common.enums.Visibility;
import me.moirai.storyengine.infrastructure.inbound.rest.validation.Moderated;

public record CreatePersonaRequest(
        @Moderated @NotEmpty(message = "cannot be empty") String name,
        @Moderated @NotEmpty(message = "cannot be empty") String personality,
        @NotNull(message = "cannot be null") Visibility visibility,
        Set<Long> usersAllowedToWrite,
        Set<Long> usersAllowedToRead) {
}
