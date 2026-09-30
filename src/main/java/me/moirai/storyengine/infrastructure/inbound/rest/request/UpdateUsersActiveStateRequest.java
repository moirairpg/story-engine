package me.moirai.storyengine.infrastructure.inbound.rest.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record UpdateUsersActiveStateRequest(
        @NotEmpty(message = "cannot be empty") List<String> usernames,
        @NotNull(message = "cannot be null") Boolean isActive) {
}
