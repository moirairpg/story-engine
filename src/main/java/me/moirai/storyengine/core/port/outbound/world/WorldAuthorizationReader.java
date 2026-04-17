package me.moirai.storyengine.core.port.outbound.world;

import java.util.Optional;
import java.util.UUID;

import me.moirai.storyengine.core.port.inbound.AssetPermissionsData;

public interface WorldAuthorizationReader {

    Optional<AssetPermissionsData> getAuthorizationData(UUID publicId);
}
