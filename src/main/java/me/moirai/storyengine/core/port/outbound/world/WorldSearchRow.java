package me.moirai.storyengine.core.port.outbound.world;

import java.time.Instant;
import java.util.UUID;

public record WorldSearchRow(
        UUID id,
        String name,
        String description,
        String visibility,
        Instant creationDate,
        String imageUrl,
        String userPermission) {
}
