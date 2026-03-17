package me.moirai.storyengine.core.port.outbound.adventure;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public record AdventureRawDto(
        UUID id,
        String name,
        String description,
        String adventureStart,
        Long worldId,
        Long personaId,
        String channelId,
        String visibility,
        String aiModel,
        String moderation,
        String gameMode,
        String ownerId,
        String nudge,
        String remember,
        String authorsNote,
        String bump,
        int bumpFrequency,
        int maxTokenLimit,
        double temperature,
        double frequencyPenalty,
        double presencePenalty,
        boolean isMultiplayer,
        OffsetDateTime creationDate,
        OffsetDateTime lastUpdateDate,
        Map<String, Double> logitBias,
        Set<String> stopSequences,
        Set<String> usersAllowedToRead,
        Set<String> usersAllowedToWrite) {
}
