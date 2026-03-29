package me.moirai.storyengine.core.port.inbound;

import java.time.Instant;
import java.util.UUID;

public record LorebookEntrySummary(
        UUID id,
        String name,
        String description,
        Instant creationDate) {}
