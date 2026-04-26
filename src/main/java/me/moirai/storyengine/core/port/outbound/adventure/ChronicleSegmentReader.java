package me.moirai.storyengine.core.port.outbound.adventure;

import java.util.List;
import java.util.UUID;

public interface ChronicleSegmentReader {
    List<ChronicleSegmentData> getAllByIds(List<UUID> publicIds);
    List<ChronicleSegmentData> getAllOrdered(UUID adventurePublicId);
}
