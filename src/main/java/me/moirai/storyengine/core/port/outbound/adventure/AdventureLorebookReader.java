package me.moirai.storyengine.core.port.outbound.adventure;

import java.util.Optional;
import java.util.UUID;

import me.moirai.storyengine.core.port.inbound.adventure.AdventureLorebookEntryDetails;

public interface AdventureLorebookReader {

    Optional<AdventureLorebookEntryDetails> getAdventureLorebookEntryById(UUID entryPublicId, UUID adventurePublicId);
}
