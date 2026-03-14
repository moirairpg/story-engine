package me.moirai.storyengine.infrastructure.security.authorization;

public interface BaseAssetAuthorizer extends BaseAuthorizer {

    boolean isOwner(String assetId, String userId);

    boolean canModify(String assetId, String userId);

    boolean canRead(String assetId, String userId);
}
