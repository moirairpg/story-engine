package me.moirai.discordbot.core.domain.notification;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import me.moirai.discordbot.common.annotation.NanoId;

@Entity(name = "NotificationRead")
@Table(name = "notification_read")
public class NotificationRead {

    @Id
    @NanoId
    private String id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "read_at", nullable = false)
    private OffsetDateTime readAt;

    protected NotificationRead() {
    }

    private NotificationRead(Builder builder) {

        this.notification = builder.notification;
        this.userId = builder.userId;
        this.readAt = builder.readAt;
    }

    static Builder builder() {

        return new Builder();
    }

    String getId() {
        return id;
    }

    Notification getNotification() {
        return notification;
    }

    public String getUserId() {
        return userId;
    }

    public OffsetDateTime getReadAt() {
        return readAt;
    }

    static final class Builder {

        private Notification notification;
        private String userId;
        private OffsetDateTime readAt;

        public Builder notification(Notification notification) {
            this.notification = notification;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder readAt(OffsetDateTime readAt) {
            this.readAt = readAt;
            return this;
        }

        NotificationRead build() {
            return new NotificationRead(this);
        }
    }
}
