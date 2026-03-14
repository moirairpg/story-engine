package me.moirai.storyengine.core.application.usecase.adventure.request;

import me.moirai.storyengine.common.usecases.UseCase;

public final class AddFavoriteAdventure extends UseCase<Void> {

    private final String assetId;
    private final String playerId;

    private AddFavoriteAdventure(Builder builder) {
        this.assetId = builder.assetId;
        this.playerId = builder.playerId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getAssetId() {
        return assetId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public static final class Builder {

        private String assetId;
        private String playerId;

        public Builder assetId(String assetId) {
            this.assetId = assetId;
            return this;
        }

        public Builder playerId(String playerId) {
            this.playerId = playerId;
            return this;
        }

        public AddFavoriteAdventure build() {
            return new AddFavoriteAdventure(this);
        }
    }
}
