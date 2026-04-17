package me.moirai.storyengine.core.port.outbound.adventure;

import java.util.Optional;
import java.util.UUID;

import me.moirai.storyengine.core.port.inbound.adventure.AdventureDetails;

public interface AdventureReader {

    Optional<AdventureDetails> getAdventureById(UUID publicId);
}
