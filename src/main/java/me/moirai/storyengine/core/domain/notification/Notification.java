package me.moirai.storyengine.core.domain.notification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import me.moirai.storyengine.common.annotation.RandomUuid;
import me.moirai.storyengine.common.domain.Asset;
import me.moirai.storyengine.common.domain.DomainEvent;
import me.moirai.storyengine.common.enums.NotificationStatus;
import me.moirai.storyengine.common.exception.BusinessRuleViolationException;

@Entity
@Table(name = "notification")
public class Notification extends Asset {

    private static final String CANNOT_DISMISS_URGENT_BROADCAST = "URGENT BROADCAST notifications cannot be dismissed";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @RandomUuid
    @Column(name = "public_id")
    private UUID publicId;

    @Column(name = "message")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "level")
    private NotificationLevel level;

    @Column(name = "target_user_id")
    private Long targetUserId;

    @Column(name = "adventure_id")
    private Long adventureId;

    @Column(name = "is_interactable")
    private boolean isInteractable;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @OneToMany(mappedBy = "notification", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<NotificationRead> reads = new ArrayList<>();

    @Transient
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    protected Notification() {
        super();
    }

    private Notification(Builder builder) {
        super();
        this.message = builder.message;
        this.type = builder.type;
        this.level = builder.level;
        this.targetUserId = builder.targetUserId;
        this.adventureId = builder.adventureId;
        this.isInteractable = builder.isInteractable;
        this.metadata = builder.metadata;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public UUID getPublicId() {
        return publicId;
    }

    public String getMessage() {
        return message;
    }

    public NotificationType getType() {
        return type;
    }

    public NotificationLevel getLevel() {
        return level;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    public Long getAdventureId() {
        return adventureId;
    }

    public boolean isInteractable() {
        return isInteractable;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void addEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    public void updateMessage(String message) {
        this.message = message;
    }

    public void updateLevel(NotificationLevel level) {
        this.level = level;
    }

    public Optional<Instant> getReadDate(Long userId) {
        return reads.stream()
                .filter(r -> r.getUserId().equals(userId))
                .map(NotificationRead::getReadDate)
                .findFirst();
    }

    public NotificationStatus getStatus(Long userId) {
        return getReadDate(userId).isPresent() ? NotificationStatus.READ : NotificationStatus.UNREAD;
    }

    public void markAsRead(Long userId) {

        if (type == NotificationType.BROADCAST && level == NotificationLevel.URGENT) {
            throw new BusinessRuleViolationException(CANNOT_DISMISS_URGENT_BROADCAST);
        }

        reads.add(NotificationRead.builder()
                .notification(this)
                .userId(userId)
                .readDate(Instant.now())
                .build());
    }

    public static final class Builder {

        private String message;
        private NotificationType type;
        private NotificationLevel level;
        private Long targetUserId;
        private Long adventureId;
        private boolean isInteractable;
        private Map<String, Object> metadata;

        private Builder() {
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder type(NotificationType type) {
            this.type = type;
            return this;
        }

        public Builder level(NotificationLevel level) {
            this.level = level;
            return this;
        }

        public Builder targetUserId(Long targetUserId) {
            this.targetUserId = targetUserId;
            return this;
        }

        public Builder adventureId(Long adventureId) {
            this.adventureId = adventureId;
            return this;
        }

        public Builder isInteractable(boolean isInteractable) {
            this.isInteractable = isInteractable;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public Notification build() {

            if (message == null || message.isBlank()) {
                throw new BusinessRuleViolationException("Notification message cannot be null or empty");
            }

            if (type == null) {
                throw new BusinessRuleViolationException("Notification type cannot be null");
            }

            if (type != NotificationType.GAME && level == null) {
                throw new BusinessRuleViolationException("Non-GAME notifications require a level");
            }

            return new Notification(this);
        }
    }
}
