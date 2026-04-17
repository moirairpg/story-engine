package me.moirai.storyengine.infrastructure.inbound.rest.request;

import jakarta.validation.constraints.NotEmpty;

public record UpdateUserUsernameRequest(
        @NotEmpty(message = "cannot be empty") String username) {
}
