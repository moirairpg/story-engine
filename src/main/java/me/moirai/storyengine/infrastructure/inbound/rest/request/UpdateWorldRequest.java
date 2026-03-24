package me.moirai.storyengine.infrastructure.inbound.rest.request;

import java.util.List;
import java.util.Set;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import me.moirai.storyengine.common.enums.Visibility;
import me.moirai.storyengine.infrastructure.inbound.rest.validation.Moderated;

public record UpdateWorldRequest(
        @Moderated @NotEmpty(message = "cannot be empty") String name,
        @Moderated @NotEmpty(message = "cannot be empty") String description,
        @Moderated @NotEmpty(message = "cannot be empty") String adventureStart,
        @NotNull(message = "cannot be null") Visibility visibility,
        Set<Long> usersAllowedToWriteToAdd,
        Set<Long> usersAllowedToWriteToRemove,
        Set<Long> usersAllowedToReadToAdd,
        Set<Long> usersAllowedToReadToRemove,
        List<AdventureLorebookEntryRequest> lorebook) {
}
