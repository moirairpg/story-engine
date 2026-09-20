package me.moirai.storyengine.infrastructure.inbound.rest.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import me.moirai.storyengine.common.enums.Role;

public record UpdateUserRequest(
        @NotNull(message = "cannot be null") Role role,
        @NotNull(message = "cannot be null") Boolean isActive,
        @Size(max = 2000, message = "cannot be longer than 2000 characters") String bio) {
}
