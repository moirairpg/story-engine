package me.moirai.storyengine.infrastructure.inbound.rest.request;

import java.util.UUID;

import me.moirai.storyengine.common.enums.PermissionLevel;

public record PermissionRequest(
        UUID userId,
        PermissionLevel level) {
}
