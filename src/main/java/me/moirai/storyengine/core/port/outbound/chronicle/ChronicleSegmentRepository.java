package me.moirai.storyengine.core.port.outbound.chronicle;

import java.util.List;
import java.util.UUID;

import me.moirai.storyengine.core.domain.chronicle.ChronicleSegment;

public interface ChronicleSegmentRepository {
    ChronicleSegment save(ChronicleSegment segment);
    List<ChronicleSegment> getAllByIds(List<UUID> publicIds);
    void deleteAllByAdventurePublicId(UUID adventurePublicId);
}
