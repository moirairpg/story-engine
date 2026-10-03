package me.moirai.storyengine.core.domain.adventure;

import java.util.UUID;

import me.moirai.storyengine.common.domain.DomainEvent;

public final class AdventureLorebookEntryRemovedEvent implements DomainEvent {

    private final UUID entryId;

    AdventureLorebookEntryRemovedEvent(UUID entryId) {

        this.entryId = entryId;
    }

    public UUID getEntryId() {
        return entryId;
    }
}
