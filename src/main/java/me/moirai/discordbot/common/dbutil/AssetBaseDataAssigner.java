package me.moirai.discordbot.common.dbutil;

import java.time.OffsetDateTime;
import java.util.Optional;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import me.moirai.discordbot.core.domain.Asset;
import me.moirai.discordbot.infrastructure.security.authentication.MoiraiPrincipal;
import me.moirai.discordbot.infrastructure.security.authentication.SecuritySessionContext;

public class AssetBaseDataAssigner {

    @PreUpdate
    @PrePersist
    public void setBaseData(Asset asset) {

        MoiraiPrincipal authenticatedUser = SecuritySessionContext.getAuthenticatedUser();
        if (asset.getCreatorDiscordId() == null) {
            String creatorName = Optional.ofNullable(authenticatedUser)
                    .map(MoiraiPrincipal::getDiscordId)
                    .orElse("SYSTEM");

            asset.setCreatorDiscordId(creatorName);
        }

        OffsetDateTime now = OffsetDateTime.now();
        if (asset.getCreationDate() == null) {
            asset.setCreationDate(now);
        }

        asset.setLastUpdateDate(now);
    }
}
