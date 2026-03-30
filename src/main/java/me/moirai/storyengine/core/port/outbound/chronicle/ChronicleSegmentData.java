package me.moirai.storyengine.core.port.outbound.chronicle;

import java.time.Instant;
import java.util.UUID;

public record ChronicleSegmentData(
        UUID publicId,
        Long adventureId,
        String content,
        Instant creationDate) {
}
