package me.moirai.storyengine.infrastructure.inbound.rest.request;

import jakarta.validation.constraints.NotEmpty;
import me.moirai.storyengine.infrastructure.inbound.rest.validation.Moderated;

public record WorldLorebookEntryRequest(
        @Moderated @NotEmpty(message = "cannot be empty") String name,
        @Moderated @NotEmpty(message = "cannot be empty") String description,
        String regex) {
}
