package me.moirai.storyengine.core.domain.message;

import java.util.UUID;

import me.moirai.storyengine.common.domain.DomainEvent;

public final class ChatMessageWindowOverflowedEvent implements DomainEvent {

    private final UUID adventurePublicId;

    ChatMessageWindowOverflowedEvent(UUID adventurePublicId) {
        this.adventurePublicId = adventurePublicId;
    }

    public UUID adventurePublicId() {
        return adventurePublicId;
    }
}
