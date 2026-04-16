package me.moirai.storyengine.core.port.inbound.world;

import java.time.Instant;
import java.util.UUID;

public record WorldSummary(
        UUID id,
        String name,
        String description,
        String visibility,
        Instant creationDate,
        String imageUrl,
        boolean canWrite) {}
