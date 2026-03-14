package me.moirai.storyengine.core.port.inbound.persona;

import me.moirai.storyengine.common.usecases.UseCase;

public final class RemoveFavoritePersona extends UseCase<Void> {

    private final String assetId;
    private final String playerId;

    private RemoveFavoritePersona(Builder builder) {
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

        public RemoveFavoritePersona build() {
            return new RemoveFavoritePersona(this);
        }
    }
}
