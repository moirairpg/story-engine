package me.moirai.storyengine.core.port.outbound.persona;

import java.util.Optional;
import java.util.UUID;

import me.moirai.storyengine.core.port.inbound.AssetPermissionsData;

public interface PersonaAuthorizationReader {

    Optional<AssetPermissionsData> getAuthorizationData(UUID publicId);
}
