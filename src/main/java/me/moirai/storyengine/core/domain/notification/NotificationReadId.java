package me.moirai.storyengine.core.domain.notification;

import java.io.Serializable;

import jakarta.persistence.Embeddable;

@Embeddable
public record NotificationReadId(Long notificationId, Long userId) implements Serializable {
}
