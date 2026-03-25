package me.moirai.storyengine.infrastructure.inbound.rest.request;

import jakarta.validation.constraints.NotNull;
import me.moirai.storyengine.common.enums.Role;

public record UpdateUserRoleRequest(
        @NotNull(message = "cannot be null") Role role) {
}
