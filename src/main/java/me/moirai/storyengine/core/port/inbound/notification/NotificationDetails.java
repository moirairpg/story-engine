package me.moirai.storyengine.core.port.inbound.notification;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = NotificationDetails.Builder.class)
public final class NotificationDetails {

    private final String id;
    private final String message;
    private final String senderDiscordId;
    private final String receiverDiscordId;
    private final String type;
    private final boolean isGlobal;
    private final boolean isInteractable;
    private final OffsetDateTime creationDate;
    private final OffsetDateTime lastUpdateDate;
    private final Map<String, Object> metadata;
    private final List<NotificationReadResult> notificationsRead;

    private NotificationDetails(Builder builder) {

        this.id = builder.id;
        this.message = builder.message;
        this.senderDiscordId = builder.senderDiscordId;
        this.receiverDiscordId = builder.receiverDiscordId;
        this.type = builder.type;
        this.isGlobal = builder.isGlobal;
        this.isInteractable = builder.isInteractable;
        this.creationDate = builder.creationDate;
        this.lastUpdateDate = builder.lastUpdateDate;

        this.metadata = unmodifiableMap(new HashMap<>(builder.metadata == null ? emptyMap() : builder.metadata));
        this.notificationsRead = unmodifiableList(
                new ArrayList<>(builder.notificationsRead == null ? emptyList() : builder.notificationsRead));
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

    public String getType() {
        return type;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public boolean isInteractable() {
        return isInteractable;
    }

    public OffsetDateTime getCreationDate() {
        return creationDate;
    }

    public OffsetDateTime getLastUpdateDate() {
        return lastUpdateDate;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public List<NotificationReadResult> getNotificationsRead() {
        return notificationsRead;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {

        private String id;
        private String message;
        private String senderDiscordId;
        private String receiverDiscordId;
        private String type;
        private boolean isGlobal;
        private boolean isInteractable;
        private OffsetDateTime creationDate;
        private OffsetDateTime lastUpdateDate;
        private Map<String, Object> metadata;
        private List<NotificationReadResult> notificationsRead;

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

        public Builder type(String type) {
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

        public Builder creationDate(OffsetDateTime creationDate) {
            this.creationDate = creationDate;
            return this;
        }

        public Builder lastUpdateDate(OffsetDateTime lastUpdateDate) {
            this.lastUpdateDate = lastUpdateDate;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder notificationsRead(List<NotificationReadResult> notificationsRead) {
            this.notificationsRead = notificationsRead;
            return this;
        }

        public NotificationDetails build() {
            return new NotificationDetails(this);
        }
    }
}
