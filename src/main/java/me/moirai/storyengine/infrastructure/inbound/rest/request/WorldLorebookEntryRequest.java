package me.moirai.storyengine.infrastructure.inbound.rest.request;

import jakarta.validation.constraints.NotEmpty;

public record WorldLorebookEntryRequest(
        @NotEmpty(message = "cannot be empty") String name,
        @NotEmpty(message = "cannot be empty") String description,
        String regex) {
}
