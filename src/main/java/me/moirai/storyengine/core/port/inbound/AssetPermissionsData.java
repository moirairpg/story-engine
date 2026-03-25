package me.moirai.storyengine.core.port.inbound;

import java.util.List;
import java.util.UUID;

import me.moirai.storyengine.common.enums.Visibility;
public record AssetPermissionsData(UUID ownerId, List<UUID> writers, List<UUID> readers, Visibility visibility) {}
