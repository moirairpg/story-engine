package me.moirai.discordbot.infrastructure.inbound.rest.response;

import java.time.OffsetDateTime;

public final class NotificationReadResponse {

    private String userId;
    private OffsetDateTime readAt;

    private NotificationReadResponse(Builder builder) {

        this.userId = builder.userId;
        this.readAt = builder.readAt;
    }

    public NotificationReadResponse() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getUserId() {
        return userId;
    }

    public OffsetDateTime getReadAt() {
        return readAt;
    }

    public static final class Builder {

        private String userId;
        private OffsetDateTime readAt;

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder readAt(OffsetDateTime readAt) {
            this.readAt = readAt;
            return this;
        }

        public NotificationReadResponse build() {
            return new NotificationReadResponse(this);
        }
    }
}
