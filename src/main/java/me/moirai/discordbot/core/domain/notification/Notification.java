package me.moirai.discordbot.core.domain.notification;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static org.apache.commons.collections4.MapUtils.isEmpty;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import me.moirai.discordbot.common.annotation.NanoId;
import me.moirai.discordbot.common.dbutil.StringObjectMapConverter;
import me.moirai.discordbot.core.domain.Asset;

@Entity(name = "Notification")
@Table(name = "notification")
public class Notification extends Asset {

    @Id
    @NanoId
    private String id;

    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "sender_discord_id", nullable = false)
    private String senderDiscordId;

    @Column(name = "receiver_discord_id", nullable = false)
    private String receiverDiscordId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;

    @Column(name = "is_global", nullable = false)
    private boolean isGlobal;

    @Column(name = "is_interactable", nullable = false)
    private boolean isInteractable;

    @Column(name = "metadata", nullable = false)
    @Convert(converter = StringObjectMapConverter.class)
    private Map<String, Object> metadata;

    @OneToMany(mappedBy = "notification", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<NotificationRead> notificationsRead;

    protected Notification() {
        super();
    }

    private Notification(Builder builder) {

        super(builder.creatorDiscordId, builder.creationDate, builder.lastUpdateDate, builder.version);

        this.id = builder.id;
        this.message = builder.message;
        this.senderDiscordId = builder.senderDiscordId;
        this.receiverDiscordId = builder.receiverDiscordId;
        this.type = builder.type;
        this.isGlobal = builder.isGlobal;
        this.isInteractable = builder.isInteractable;

        this.metadata = new HashMap<>(isEmpty(builder.metadata) ? emptyMap() : builder.metadata);

        this.notificationsRead = new HashSet<>();
    }

    public static Builder builder() {

        return new Builder();
    }

    public String getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public String getSenderDiscordId() {
        return senderDiscordId;
    }

    public String getReceiverDiscordId() {
        return receiverDiscordId;
    }

    public NotificationType getType() {
        return type;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public boolean isInteractable() {
        return isInteractable;
    }

    public Map<String, Object> getMetadata() {
        return unmodifiableMap(metadata);
    }

    public Set<NotificationRead> getNotificationsRead() {
        return unmodifiableSet(notificationsRead);
    }

    public boolean isReadByUserId(String userId) {

        return notificationsRead.stream()
                .anyMatch(readData -> readData.getUserId().equals(userId));
    }

    public Optional<OffsetDateTime> getReadAtByUserId(String userId) {

        return notificationsRead.stream()
                .filter(readData -> readData.getUserId().equals(userId))
                .findAny()
                .map(NotificationRead::getReadAt);

    }

    public OffsetDateTime markAsRead(String userId) {

        OffsetDateTime readAt = OffsetDateTime.now();
        notificationsRead.add(NotificationRead.builder()
                .notification(this)
                .userId(userId)
                .readAt(readAt)
                .build());

        return readAt;
    }

    public static final class Builder {

        private String id;
        private String message;
        private String senderDiscordId;
        private String receiverDiscordId;
        private NotificationType type;
        private boolean isGlobal;
        private boolean isInteractable;
        private Map<String, Object> metadata;
        private String creatorDiscordId;
        private OffsetDateTime creationDate;
        private OffsetDateTime lastUpdateDate;
        private int version;

        public Builder id(String id) {

            this.id = id;
            return this;
        }

        public Builder message(String message) {

            this.message = message;
            return this;
        }

        public Builder senderDiscordId(String senderDiscordId) {

            this.senderDiscordId = senderDiscordId;
            return this;
        }

        public Builder receiverDiscordId(String receiverDiscordId) {

            this.receiverDiscordId = receiverDiscordId;
            return this;
        }

        public Builder type(NotificationType type) {

            this.type = type;
            return this;
        }

        public Builder isGlobal(boolean isGlobal) {

            this.isGlobal = isGlobal;
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

        public Builder creatorDiscordId(String creatorDiscordId) {

            this.creatorDiscordId = creatorDiscordId;
            return this;
        }

        public Builder creationDate(OffsetDateTime creationDate) {

            this.creationDate = creationDate;
            return this;
        }

        public Builder lastUpdateDate(OffsetDateTime lastUpdateDate) {

            this.lastUpdateDate = lastUpdateDate;
            return this;
        }

        public Builder version(int version) {

            this.version = version;
            return this;
        }

        public Notification build() {
            return new Notification(this);
        }
    }
}
