package me.moirai.storyengine.core.port.inbound.world;

import me.moirai.storyengine.common.usecases.UseCase;

public final class SearchWorlds extends UseCase<SearchWorldsResult> {

    private final String name;
    private final String ownerId;
    private final Integer page;
    private final Integer size;
    private final String sortingField;
    private final String direction;
    private final String visibility;
    private final String operation;
    private final String requesterId;

    private SearchWorlds(Builder builder) {

        this.name = builder.name;
        this.ownerId = builder.ownerId;
        this.page = builder.page;
        this.size = builder.size;
        this.sortingField = builder.sortingField;
        this.direction = builder.direction;
        this.visibility = builder.visibility;
        this.operation = builder.operation;
        this.requesterId = builder.requesterId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getName() {
        return name;
    }

    public String getOwnerId() {
        return ownerId;
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

    public String getVisibility() {
        return visibility;
    }

    public String getOperation() {
        return operation;
    }

    public String getRequesterDiscordId() {
        return requesterId;
    }

    public static final class Builder {

        private String name;
        private String ownerId;
        private Integer page;
        private Integer size;
        private String sortingField;
        private String direction;
        private String visibility;
        private String operation;
        private String requesterId;

        private Builder() {
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder ownerId(String ownerId) {
            this.ownerId = ownerId;
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

        public Builder visibility(String visibility) {
            this.visibility = visibility;
            return this;
        }

        public Builder operation(String operation) {
            this.operation = operation;
            return this;
        }

        public Builder requesterId(String requesterId) {
            this.requesterId = requesterId;
            return this;
        }

        public SearchWorlds build() {
            return new SearchWorlds(this);
        }
    }
}