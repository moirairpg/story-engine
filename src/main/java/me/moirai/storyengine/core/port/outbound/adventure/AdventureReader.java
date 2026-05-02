package me.moirai.storyengine.core.port.outbound.adventure;

import java.util.Optional;
import java.util.UUID;

public interface AdventureReader {

    Optional<AdventureDetailsRow> getAdventureById(UUID publicId);
}
