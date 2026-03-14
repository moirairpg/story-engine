package me.moirai.storyengine.infrastructure.outbound.adapter.favorite;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import me.moirai.storyengine.common.annotation.UuidIdentifier;

@Entity(name = "Favorite")
@Table(name = "favorite")
public class FavoriteEntity {

    @Id
    @UuidIdentifier
    private String id;

    @Column(name = "player_discord_id", nullable = false)
    private String playerId;

    @Column(name = "asset_id", nullable = false)
    private String assetId;

    @Column(name = "asset_type", nullable = false)
    private String assetType;

    private FavoriteEntity(Builder builder) {
        this.playerId = builder.playerId;
        this.assetId = builder.assetId;
        this.assetType = builder.assetType;
    }

    protected FavoriteEntity() {
        super();
    }

    public static Builder builder() {

        return new Builder();
    }

    public String getId() {
        return id;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getAssetId() {
        return assetId;
    }

    public String getAssetType() {
        return assetType;
    }

    public static final class Builder {

        private String playerId;
        private String assetId;
        private String assetType;

        public Builder playerId(String playerId) {
            this.playerId = playerId;
            return this;
        }

        public Builder assetId(String assetId) {
            this.assetId = assetId;
            return this;
        }

        public Builder assetType(String assetType) {
            this.assetType = assetType;
            return this;
        }

        public FavoriteEntity build() {

            return new FavoriteEntity(this);
        }
    }
}
