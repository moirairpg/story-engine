package me.moirai.storyengine.core.port.inbound.adventure;

import java.time.Instant;
import java.util.UUID;

public record AdventureSummary(
        UUID id,
        String name,
        String description,
        String worldName,
        String personaName,
        String visibility,
        Instant creationDate) {
}
