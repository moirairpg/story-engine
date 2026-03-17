package me.moirai.storyengine.core.port.inbound.adventure;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import me.moirai.storyengine.common.cqs.command.Command;

public record CreateAdventure(
        String name,
        String description,
        UUID worldId,
        UUID personaId,
        String channelId,
        String visibility,
        String aiModel,
        String moderation,
        String requesterId,
        String gameMode,
        String nudge,
        String remember,
        String authorsNote,
        String bump,
        Integer bumpFrequency,
        Integer maxTokenLimit,
        Double temperature,
        Double frequencyPenalty,
        Double presencePenalty,
        Map<String, Double> logitBias,
        Set<String> stopSequences,
        Set<String> usersAllowedToWrite,
        Set<String> usersAllowedToRead,
        boolean isMultiplayer)
        implements Command<AdventureDetails> {
}
