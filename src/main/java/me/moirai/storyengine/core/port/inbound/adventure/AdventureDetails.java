package me.moirai.storyengine.core.port.inbound.adventure;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public record AdventureDetails(
        UUID id,
        String name,
        String description,
        String adventureStart,
        UUID worldId,
        UUID personaId,
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
        Instant creationDate,
        Instant lastUpdateDate,
        Map<String, Double> logitBias,
        Set<String> stopSequences,
        Set<String> usersAllowedToRead,
        Set<String> usersAllowedToWrite) {

    public AdventureDetails {
        usersAllowedToWrite = Set.copyOf(usersAllowedToWrite);
        usersAllowedToRead = Set.copyOf(usersAllowedToRead);
    }
}
