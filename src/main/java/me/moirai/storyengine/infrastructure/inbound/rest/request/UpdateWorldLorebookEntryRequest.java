package me.moirai.storyengine.infrastructure.inbound.rest.request;

import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record UpdateWorldLorebookEntryRequest(
        @NotNull(message = "cannot be null") UUID id,
        @NotEmpty(message = "cannot be empty") String name,
        @NotEmpty(message = "cannot be empty") String description) implements LorebookEntryModerationSource {
}
