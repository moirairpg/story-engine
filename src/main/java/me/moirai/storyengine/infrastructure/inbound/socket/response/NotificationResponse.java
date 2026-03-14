package me.moirai.storyengine.infrastructure.inbound.socket.response;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;

import java.util.HashMap;
import java.util.Map;

public final class NotificationResponse {

    private String message;
    private String senderDiscordId;
    private String receiverDiscordId;
    private String type;
    private boolean isGlobal;
    private boolean isInteractable;
    private Map<String, Object> metadata;

    private NotificationResponse(Builder builder) {

        this.message = builder.message;
        this.senderDiscordId = builder.senderDiscordId;
        this.receiverDiscordId = builder.receiverDiscordId;
        this.type = builder.type;
        this.isGlobal = builder.isGlobal;
        this.isInteractable = builder.isInteractable;

        this.metadata = unmodifiableMap(new HashMap<>(builder.metadata == null ? emptyMap() : builder.metadata));
    }

    public NotificationResponse() {
    }

    public static Builder builder() {
        return new Builder();
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

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public static final class Builder {

        private String message;
        private String senderDiscordId;
        private String receiverDiscordId;
        private String type;
        private boolean isGlobal;
        private boolean isInteractable;
        private Map<String, Object> metadata;

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

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public NotificationResponse build() {
            return new NotificationResponse(this);
        }
    }
}