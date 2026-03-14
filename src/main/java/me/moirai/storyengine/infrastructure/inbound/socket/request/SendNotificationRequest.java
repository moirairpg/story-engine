package me.moirai.storyengine.infrastructure.inbound.socket.request;

import java.util.Map;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class SendNotificationRequest {

    @NotEmpty(message = "cannot be empty")
    @NotNull(message = "cannot be null")
    private String message;

    @NotEmpty(message = "cannot be empty")
    @NotNull(message = "cannot be null")
    private String senderDiscordId;
    private String receiverDiscordId;

    @NotEmpty(message = "cannot be empty")
    @NotNull(message = "cannot be null")
    private String type;
    private boolean isGlobal;
    private boolean isInteractable;
    private Map<String, Object> metadata;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderDiscordId() {
        return senderDiscordId;
    }

    public void setSenderDiscordId(String senderDiscordId) {
        this.senderDiscordId = senderDiscordId;
    }

    public String getReceiverDiscordId() {
        return receiverDiscordId;
    }

    public void setReceiverDiscordId(String receiverDiscordId) {
        this.receiverDiscordId = receiverDiscordId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean getIsGlobal() {
        return isGlobal;
    }

    public void setIsGlobal(boolean isGlobal) {
        this.isGlobal = isGlobal;
    }

    public boolean getIsInteractable() {
        return isInteractable;
    }

    public void setIsInteractable(boolean isInteractable) {
        this.isInteractable = isInteractable;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
