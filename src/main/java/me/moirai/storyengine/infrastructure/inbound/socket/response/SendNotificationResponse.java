package me.moirai.storyengine.infrastructure.inbound.socket.response;

import java.time.OffsetDateTime;

public class SendNotificationResponse {

    private final String id;
    private final OffsetDateTime creationDateTime;

    public SendNotificationResponse(String id, OffsetDateTime creationDateTime) {
        this.id = id;
        this.creationDateTime = creationDateTime;
    }

    public static SendNotificationResponse withIdAndCreationDateTime(String id, OffsetDateTime creationDateTime) {
        return new SendNotificationResponse(id, creationDateTime);
    }

    public String getId() {
        return id;
    }

    public OffsetDateTime getCreationDateTime() {
        return creationDateTime;
    }
}
