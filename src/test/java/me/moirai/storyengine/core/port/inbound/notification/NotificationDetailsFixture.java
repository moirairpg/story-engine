package me.moirai.storyengine.core.port.inbound.notification;

import java.time.Instant;
import java.util.UUID;

import me.moirai.storyengine.common.enums.NotificationStatus;
import me.moirai.storyengine.core.domain.notification.NotificationLevel;
import me.moirai.storyengine.core.domain.notification.NotificationType;

public class NotificationDetailsFixture {

    public static NotificationDetails broadcast() {

        return new NotificationDetails(
                UUID.fromString("857345aa-6666-0000-0000-000000000001"),
                "Broadcast message",
                NotificationType.BROADCAST,
                NotificationLevel.INFO,
                NotificationStatus.UNREAD,
                null,
                null,
                false,
                null,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z"));
    }

    public static NotificationDetails system() {

        return new NotificationDetails(
                UUID.fromString("857345aa-6666-0000-0000-000000000002"),
                "System message",
                NotificationType.SYSTEM,
                NotificationLevel.INFO,
                NotificationStatus.UNREAD,
                "some_user",
                null,
                false,
                null,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z"));
    }
}
