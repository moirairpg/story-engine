package me.moirai.storyengine.core.domain.notification;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import me.moirai.storyengine.common.exception.BusinessRuleViolationException;

@Entity
@Table(name = "notification_read")
public class NotificationRead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "read_date")
    private Instant readDate;

    protected NotificationRead() {
        super();
    }

    private NotificationRead(Builder builder) {
        super();
        this.notification = builder.notification;
        this.userId = builder.userId;
        this.readDate = builder.readDate;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public Notification getNotification() {
        return notification;
    }

    public Long getNotificationId() {
        return notification != null ? notification.getId() : null;
    }

    public Long getUserId() {
        return userId;
    }

    public Instant getReadDate() {
        return readDate;
    }

    public static final class Builder {

        private Notification notification;
        private Long userId;
        private Instant readDate;

        private Builder() {
        }

        public Builder notification(Notification notification) {
            this.notification = notification;
            return this;
        }

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder readDate(Instant readDate) {
            this.readDate = readDate;
            return this;
        }

        public NotificationRead build() {

            if (notification == null) {
                throw new BusinessRuleViolationException("Notification is required");
            }

            if (userId == null) {
                throw new BusinessRuleViolationException("User ID is required");
            }

            if (readDate == null) {
                throw new BusinessRuleViolationException("Read timestamp is required");
            }

            return new NotificationRead(this);
        }
    }
}
