package me.moirai.storyengine.core.port.inbound;

import java.util.UUID;

import me.moirai.storyengine.common.enums.PermissionLevel;

public record AssetMember(UUID userId, String username, String displayName, PermissionLevel level) {}
