package me.moirai.storyengine.core.port.outbound.chronicle;

import me.moirai.storyengine.core.domain.chronicle.ChronicleSegment;

public interface ChronicleSegmentRepository {
    ChronicleSegment save(ChronicleSegment segment);
}
