package me.moirai.storyengine.core.port.outbound.world;

import java.util.Optional;
import java.util.UUID;

public interface WorldReader {

    Optional<WorldDetailsRow> getWorldById(UUID publicId);
}
