package me.moirai.storyengine.core.port.inbound.persona;

import java.time.Instant;
import java.util.UUID;

public record PersonaSummary(
        UUID id,
        String name,
        String personality,
        String visibility,
        Instant creationDate) {}
