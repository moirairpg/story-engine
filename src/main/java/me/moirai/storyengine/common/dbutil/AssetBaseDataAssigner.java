package me.moirai.storyengine.common.dbutil;

import java.time.Instant;
import java.util.Optional;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import me.moirai.storyengine.common.domain.Asset;
import me.moirai.storyengine.common.domain.Permission;
import me.moirai.storyengine.common.domain.ShareableAsset;
import me.moirai.storyengine.common.security.authentication.MoiraiPrincipal;
import me.moirai.storyengine.common.security.authentication.MoiraiSecurityContext;

public class AssetBaseDataAssigner {

    @PreUpdate
    @PrePersist
    public void setBaseData(Asset asset) {

        var authenticatedUser = MoiraiSecurityContext.getAuthenticatedUser();
        if (asset.getCreatedBy() == null) {
            var createdBy = Optional.ofNullable(authenticatedUser)
                    .map(MoiraiPrincipal::username)
                    .orElse("SYSTEM");

            asset.setCreatedBy(createdBy);
        }

        var now = Instant.now();
        if (asset.getCreationDate() == null) {
            asset.setCreationDate(now);
        }

        if (asset instanceof ShareableAsset shareableAsset && authenticatedUser != null) {
            if (shareableAsset.getPermissions().stream()
                    .noneMatch(p -> p.level() == me.moirai.storyengine.common.enums.PermissionLevel.OWNER)) {

                shareableAsset.grant(
                        new Permission(authenticatedUser.id(),
                                me.moirai.storyengine.common.enums.PermissionLevel.OWNER));
            }
        }

        asset.setLastUpdateDate(now);
    }
}
