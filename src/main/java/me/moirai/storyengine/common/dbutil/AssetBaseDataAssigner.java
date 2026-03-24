package me.moirai.storyengine.common.dbutil;

import java.time.Instant;
import java.util.Optional;

import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import me.moirai.storyengine.common.domain.Asset;
import me.moirai.storyengine.common.domain.Permission;
import me.moirai.storyengine.common.domain.ShareableAsset;
import me.moirai.storyengine.common.security.authentication.MoiraiPrincipal;

public class AssetBaseDataAssigner {

    @PreUpdate
    @PrePersist
    public void setBaseData(Asset asset) {

        var authenticatedUser = getAuthenticatedUser();
        if (asset.getCreatorId() == null) {
            var creatorName = Optional.ofNullable(authenticatedUser)
                    .map(MoiraiPrincipal::discordId)
                    .orElse("SYSTEM");

            asset.setCreatorId(creatorName);
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

    private MoiraiPrincipal getAuthenticatedUser() {

        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(auth -> (MoiraiPrincipal) auth.getPrincipal())
                .orElse(null);
    }
}
