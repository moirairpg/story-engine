package me.moirai.storyengine.core.port.outbound.world;

import java.util.Optional;
import java.util.UUID;

import me.moirai.storyengine.core.port.inbound.world.WorldDetails;

public interface WorldReader {

    Optional<WorldDetails> getWorldById(UUID publicId);
}
