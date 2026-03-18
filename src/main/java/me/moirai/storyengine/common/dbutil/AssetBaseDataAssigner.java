package me.moirai.storyengine.common.dbutil;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import me.moirai.storyengine.common.domain.Asset;
import me.moirai.storyengine.infrastructure.security.authentication.MoiraiPrincipal;

public class AssetBaseDataAssigner {

    @PreUpdate
    @PrePersist
    public void setBaseData(Asset asset) {

        MoiraiPrincipal authenticatedUser = getAuthenticatedUser();
        if (asset.getCreatorId() == null) {
            String creatorName = Optional.ofNullable(authenticatedUser)
                    .map(MoiraiPrincipal::discordId)
                    .orElse("SYSTEM");

            asset.setCreatorId(creatorName);
        }

        OffsetDateTime now = OffsetDateTime.now();
        if (asset.getCreationDate() == null) {
            asset.setCreationDate(now);
        }

        asset.setLastUpdateDate(now);
    }

    private MoiraiPrincipal getAuthenticatedUser() {

        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(auth -> (MoiraiPrincipal) auth.getPrincipal())
                .orElse(null);
    }
}
