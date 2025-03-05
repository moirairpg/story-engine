package me.moirai.discordbot.core.domain.notification;

import java.time.OffsetDateTime;

public class NotificationReadFixture {

    public static NotificationRead.Builder read() {

        return NotificationRead.builder()
                .readAt(OffsetDateTime.parse("2025-01-02T12:00:00.000000Z"))
                .userId("12345");
    }
}
