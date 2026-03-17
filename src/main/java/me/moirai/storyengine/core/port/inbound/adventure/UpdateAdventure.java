package me.moirai.storyengine.core.port.inbound.adventure;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import me.moirai.storyengine.common.cqs.command.Command;

public record UpdateAdventure(
        UUID adventureId,
        String description,
        String adventureStart,
        String name,
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
        Map<String, Double> logitBiasToAdd,
        Set<String> stopSequencesToAdd,
        Set<String> stopSequencesToRemove,
        Set<String> logitBiasToRemove,
        Set<String> usersAllowedToWriteToAdd,
        Set<String> usersAllowedToWriteToRemove,
        Set<String> usersAllowedToReadToAdd,
        Set<String> usersAllowedToReadToRemove,
        boolean isMultiplayer)
        implements Command<AdventureDetails> {
}
