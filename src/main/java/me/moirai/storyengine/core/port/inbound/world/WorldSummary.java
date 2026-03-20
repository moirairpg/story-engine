package me.moirai.storyengine.core.port.inbound.world;

import java.time.OffsetDateTime;
import java.util.UUID;

public record WorldSummary(
        UUID id,
        String name,
        String description,
        String visibility,
        OffsetDateTime creationDate) {}
