package me.moirai.storyengine.core.application.usecase.notification.result;

import java.time.OffsetDateTime;

public final class NotificationReadResult {

    private final String userId;
    private final OffsetDateTime readAt;

    private NotificationReadResult(Builder builder) {

        this.userId = builder.userId;
        this.readAt = builder.readAt;
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

        public NotificationReadResult build() {
            return new NotificationReadResult(this);
        }
    }
}
