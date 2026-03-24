package me.moirai.storyengine.core.port.inbound.adventure;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import me.moirai.storyengine.common.cqs.command.Command;
import me.moirai.storyengine.common.enums.ArtificialIntelligenceModel;
import me.moirai.storyengine.common.enums.GameMode;
import me.moirai.storyengine.common.enums.Moderation;
import me.moirai.storyengine.common.enums.Visibility;

public record CreateAdventure(
        String name,
        String description,
        UUID worldId,
        UUID personaId,
        String channelId,
        Visibility visibility,
        ArtificialIntelligenceModel aiModel,
        Moderation moderation,
        String requesterId,
        GameMode gameMode,
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
        Set<Long> usersAllowedToWrite,
        Set<Long> usersAllowedToRead,
        boolean isMultiplayer)
        implements Command<AdventureDetails> {
}
