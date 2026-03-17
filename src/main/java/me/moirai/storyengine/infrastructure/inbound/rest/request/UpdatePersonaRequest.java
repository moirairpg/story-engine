package me.moirai.storyengine.infrastructure.inbound.rest.request;

import java.util.Set;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import me.moirai.storyengine.common.enums.Visibility;

public record UpdatePersonaRequest(
        @NotEmpty(message = "cannot be empty") String name,
        @NotEmpty(message = "cannot be empty") String personality,
        @NotNull(message = "cannot be null") Visibility visibility,
        Set<String> usersAllowedToWriteToAdd,
        Set<String> usersAllowedToWriteToRemove,
        Set<String> usersAllowedToReadToAdd,
        Set<String> usersAllowedToReadToRemove) {
}