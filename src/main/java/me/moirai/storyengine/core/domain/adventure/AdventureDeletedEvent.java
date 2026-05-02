package me.moirai.storyengine.core.domain.adventure;

import java.util.UUID;

import me.moirai.storyengine.common.domain.DomainEvent;

public final class AdventureDeletedEvent implements DomainEvent {

    private final UUID adventurePublicId;

    AdventureDeletedEvent(UUID adventurePublicId) {
        this.adventurePublicId = adventurePublicId;
    }

    public UUID getAdventureId() {
        return adventurePublicId;
    }
}
