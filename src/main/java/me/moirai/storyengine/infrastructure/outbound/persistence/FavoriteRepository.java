package me.moirai.storyengine.infrastructure.outbound.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRepository extends JpaRepository<FavoriteEntity, String> {

    void deleteByPlayerIdAndAssetIdAndAssetType(String playerId, String assetId, String assetType);

    void deleteAllByAssetId(String assetId);
}
