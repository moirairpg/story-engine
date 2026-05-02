package me.moirai.storyengine.core.domain.adventure;

import me.moirai.storyengine.common.domain.DomainEvent;

public final class AdventureDeletedEvent implements DomainEvent {

    private final Long adventureId;

    AdventureDeletedEvent(Long adventureId) {
        this.adventureId = adventureId;
    }

    public Long getAdventureId() {
        return adventureId;
    }
}
