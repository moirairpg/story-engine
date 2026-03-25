package me.moirai.storyengine.core.port.outbound.adventure;

import java.util.Optional;
import java.util.UUID;

import me.moirai.storyengine.core.port.inbound.AssetPermissionsData;

public interface AdventureAuthorizationReader {

    Optional<AssetPermissionsData> getAuthorizationData(UUID publicId);
}
