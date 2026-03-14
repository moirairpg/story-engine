package me.moirai.storyengine.core.application.usecase.notification.request;

import me.moirai.storyengine.common.usecases.UseCase;
import me.moirai.storyengine.core.application.usecase.notification.result.SearchNotificationsResult;

public final class SearchNotifications extends UseCase<SearchNotificationsResult> {

    private final String senderDiscordId;
    private final String receiverDiscordId;
    private final String type;
    private final Boolean isGlobal;
    private final Boolean isInteractable;
    private final Integer page;
    private final Integer size;
    private final String sortingField;
    private final String direction;

    private SearchNotifications(Builder builder) {

        this.senderDiscordId = builder.senderDiscordId;
        this.receiverDiscordId = builder.receiverDiscordId;
        this.type = builder.type;
        this.isGlobal = builder.isGlobal;
        this.isInteractable = builder.isInteractable;
        this.page = builder.page;
        this.size = builder.size;
        this.sortingField = builder.sortingField;
        this.direction = builder.direction;
    }

    public static Builder builder() {
        return new Builder();
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

    public Boolean isGlobal() {
        return isGlobal;
    }

    public Boolean isInteractable() {
        return isInteractable;
    }

    public Integer getPage() {
        return page;
    }

    public Integer getSize() {
        return size;
    }

    public String getSortingField() {
        return sortingField;
    }

    public String getDirection() {
        return direction;
    }

    public static final class Builder {

        private String senderDiscordId;
        private String receiverDiscordId;
        private String type;
        private Boolean isGlobal;
        private Boolean isInteractable;
        private Integer page;
        private Integer size;
        private String sortingField;
        private String direction;

        private Builder() {
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

        public Builder isGlobal(Boolean isGlobal) {
            this.isGlobal = isGlobal;
            return this;
        }

        public Builder isInteractable(Boolean isInteractable) {
            this.isInteractable = isInteractable;
            return this;
        }

        public Builder page(Integer page) {
            this.page = page;
            return this;
        }

        public Builder size(Integer size) {
            this.size = size;
            return this;
        }

        public Builder sortingField(String sortingField) {
            this.sortingField = sortingField;
            return this;
        }

        public Builder direction(String direction) {
            this.direction = direction;
            return this;
        }

        public SearchNotifications build() {
            return new SearchNotifications(this);
        }
    }
}