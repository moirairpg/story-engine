package me.moirai.storyengine.core.port.inbound.persona;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PersonaSummary(
        UUID id,
        String name,
        String personality,
        String visibility,
        OffsetDateTime creationDate) {}
