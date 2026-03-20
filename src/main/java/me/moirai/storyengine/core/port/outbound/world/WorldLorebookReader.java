package me.moirai.storyengine.core.port.outbound.world;

import java.util.Optional;
import java.util.UUID;

import me.moirai.storyengine.core.port.inbound.world.WorldLorebookEntryDetails;

public interface WorldLorebookReader {

    Optional<WorldLorebookEntryDetails> getWorldLorebookEntryById(UUID entryPublicId, UUID worldPublicId);
}
