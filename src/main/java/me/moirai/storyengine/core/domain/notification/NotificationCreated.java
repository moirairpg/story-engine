package me.moirai.storyengine.core.domain.notification;

import java.util.UUID;

import me.moirai.storyengine.common.domain.DomainEvent;

public record NotificationCreated(UUID publicId) implements DomainEvent {
}
