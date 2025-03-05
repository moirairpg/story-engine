package me.moirai.discordbot.core.application.usecase.notification.result;

import java.time.OffsetDateTime;

public final class SendNotificationResult {

    private final String id;
    private final OffsetDateTime creationDateTime;

    public SendNotificationResult(String id, OffsetDateTime creationDateTime) {
        this.id = id;
        this.creationDateTime = creationDateTime;
    }

    public static SendNotificationResult withIdAndCreationDateTime(String id, OffsetDateTime creationDateTime) {
        return new SendNotificationResult(id, creationDateTime);
    }

    public String getId() {
        return id;
    }

    public OffsetDateTime getCreationDateTime() {
        return creationDateTime;
    }
}
