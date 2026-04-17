package me.moirai.storyengine.core.port.outbound.adventure;

import java.time.Instant;
import java.util.UUID;

public record AdventureSearchRow(
        UUID id,
        String name,
        String description,
        String worldName,
        String narratorName,
        String visibility,
        Instant creationDate,
        String imageUrl,
        String userPermission) {
}
