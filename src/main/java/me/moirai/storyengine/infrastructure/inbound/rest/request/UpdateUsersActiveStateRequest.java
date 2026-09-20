package me.moirai.storyengine.infrastructure.inbound.rest.request;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record UpdateUsersActiveStateRequest(
        List<UUID> userIds,
        @NotNull(message = "cannot be null") Boolean isActive) {
}
