package me.moirai.storyengine.common.dbutil;

import java.time.OffsetDateTime;
import java.util.Optional;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import me.moirai.storyengine.common.domain.Asset;
import me.moirai.storyengine.infrastructure.security.authentication.MoiraiPrincipal;
import me.moirai.storyengine.infrastructure.security.authentication.SecuritySessionContext;

public class AssetBaseDataAssigner {

    @PreUpdate
    @PrePersist
    public void setBaseData(Asset asset) {

        MoiraiPrincipal authenticatedUser = SecuritySessionContext.getAuthenticatedUser();
        if (asset.getCreatorId() == null) {
            String creatorName = Optional.ofNullable(authenticatedUser)
                    .map(MoiraiPrincipal::getDiscordId)
                    .orElse("SYSTEM");

            asset.setCreatorId(creatorName);
        }

        OffsetDateTime now = OffsetDateTime.now();
        if (asset.getCreationDate() == null) {
            asset.setCreationDate(now);
        }

        asset.setLastUpdateDate(now);
    }
}
