package me.moirai.storyengine.core.port.outbound.adventure;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import me.moirai.storyengine.common.dto.PermissionDto;
import me.moirai.storyengine.common.enums.Moderation;
import me.moirai.storyengine.common.enums.Visibility;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureLorebookEntryDetails;
import me.moirai.storyengine.core.port.inbound.adventure.ContextAttributesDto;
import me.moirai.storyengine.core.port.inbound.adventure.ModelConfigurationDto;

public record AdventureDetailsRow(
        UUID id,
        String name,
        String description,
        String adventureStart,
        UUID worldId,
        String narratorName,
        String narratorPersonality,
        Visibility visibility,
        Moderation moderation,
        boolean isMultiplayer,
        String imageKey,
        Instant creationDate,
        Instant lastUpdateDate,
        ModelConfigurationDto modelConfiguration,
        ContextAttributesDto contextAttributes,
        Set<PermissionDto> permissions,
        Set<AdventureLorebookEntryDetails> lorebook,
        Double uiImagePositionX,
        Double uiImagePositionY) {

    public AdventureDetailsRow {
        permissions = Set.copyOf(permissions);
        lorebook = Set.copyOf(lorebook);
    }
}
