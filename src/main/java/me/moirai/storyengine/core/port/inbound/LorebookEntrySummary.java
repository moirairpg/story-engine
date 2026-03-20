package me.moirai.storyengine.core.port.inbound;

import java.time.OffsetDateTime;
import java.util.UUID;

public record LorebookEntrySummary(
        UUID id,
        String name,
        String regex,
        String description,
        OffsetDateTime creationDate) {}
