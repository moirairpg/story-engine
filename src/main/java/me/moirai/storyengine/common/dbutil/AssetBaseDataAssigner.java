package me.moirai.storyengine.common.dbutil;

import java.time.Instant;
import java.util.Optional;

import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import me.moirai.storyengine.common.domain.Asset;
import me.moirai.storyengine.common.security.authentication.MoiraiPrincipal;

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

        Instant now = Instant.now();
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
