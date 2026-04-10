package me.moirai.storyengine.core.port.outbound.persona;

import java.time.Instant;
import java.util.UUID;

public record PersonaSearchRow(
        UUID id,
        String name,
        String personality,
        String visibility,
        Instant creationDate,
        String userPermission) {
}
