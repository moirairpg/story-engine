package me.moirai.storyengine.core.port.inbound.adventure;

import me.moirai.storyengine.common.usecases.UseCase;

public final class SearchAdventures extends UseCase<SearchAdventuresResult> {

    private final String name;
    private final String world;
    private final String persona;
    private final String ownerId;
    private final boolean favorites;
    private final boolean isMultiplayer;
    private final Integer page;
    private final Integer size;
    private final String model;
    private final String gameMode;
    private final String moderation;
    private final String sortingField;
    private final String direction;
    private final String visibility;
    private final String operation;
    private final String requesterId;

    private SearchAdventures(Builder builder) {

        this.name = builder.name;
        this.world = builder.world;
        this.persona = builder.persona;
        this.ownerId = builder.ownerId;
        this.favorites = builder.favorites;
        this.isMultiplayer = builder.isMultiplayer;
        this.page = builder.page;
        this.size = builder.size;
        this.model = builder.model;
        this.gameMode = builder.gameMode;
        this.moderation = builder.moderation;
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

    public String getWorld() {
        return world;
    }

    public String getPersona() {
        return persona;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public boolean isFavorites() {
        return favorites;
    }

    public boolean isMultiplayer() {
        return isMultiplayer;
    }

    public Integer getPage() {
        return page;
    }

    public Integer getSize() {
        return size;
    }

    public String getModel() {
        return model;
    }

    public String getGameMode() {
        return gameMode;
    }

    public String getModeration() {
        return moderation;
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
        private String world;
        private String persona;
        private String ownerId;
        private boolean favorites;
        private boolean isMultiplayer;
        private Integer page;
        private Integer size;
        private String model;
        private String gameMode;
        private String moderation;
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

        public Builder world(String world) {
            this.world = world;
            return this;
        }

        public Builder persona(String persona) {
            this.persona = persona;
            return this;
        }

        public Builder ownerId(String ownerId) {
            this.ownerId = ownerId;
            return this;
        }

        public Builder favorites(boolean favorites) {
            this.favorites = favorites;
            return this;
        }

        public Builder multiplayer(boolean isMultiplayer) {
            this.isMultiplayer = isMultiplayer;
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

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public Builder gameMode(String gameMode) {
            this.gameMode = gameMode;
            return this;
        }

        public Builder moderation(String moderation) {
            this.moderation = moderation;
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

        public SearchAdventures build() {
            return new SearchAdventures(this);
        }
    }
}