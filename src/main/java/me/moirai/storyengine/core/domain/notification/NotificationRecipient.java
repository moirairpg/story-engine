package me.moirai.storyengine.core.domain.notification;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import me.moirai.storyengine.common.exception.BusinessRuleViolationException;

@Entity
@Table(name = "notification_recipient")
public class NotificationRecipient {

    @EmbeddedId
    private NotificationRecipientId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("notificationId")
    @JoinColumn(name = "notification_id")
    private Notification notification;

    protected NotificationRecipient() {
        super();
    }

    private NotificationRecipient(Builder builder) {
        super();
        this.notification = builder.notification;
        this.id = new NotificationRecipientId(null, builder.userId);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Notification getNotification() {
        return notification;
    }

    public Long getUserId() {
        return id.userId();
    }

    public static final class Builder {

        private Notification notification;
        private Long userId;

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

        public NotificationRecipient build() {

            if (notification == null) {
                throw new BusinessRuleViolationException("Notification is required");
            }

            if (userId == null) {
                throw new BusinessRuleViolationException("User ID is required");
            }

            return new NotificationRecipient(this);
        }
    }
}
