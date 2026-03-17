package me.moirai.storyengine.core.application.usecase.adventure.request;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.assertj.core.util.Maps;

import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventure;

public class UpdateAdventureFixture {

    public static UpdateAdventure sample() {

        Adventure adventure = AdventureFixture.privateMultiplayerAdventure().build();
        return new UpdateAdventure(
                AdventureFixture.PUBLIC_ID,
                adventure.getDescription(),
                adventure.getAdventureStart(),
                adventure.getName(),
                WorldFixture.PUBLIC_ID,
                PersonaFixture.PUBLIC_ID,
                adventure.getChannelId(),
                adventure.getVisibility().name(),
                adventure.getModelConfiguration().aiModel().toString(),
                adventure.getModeration().name(),
                adventure.getOwnerId(),
                adventure.getGameMode().name(),
                adventure.getContextAttributes().nudge(),
                adventure.getContextAttributes().remember(),
                adventure.getContextAttributes().authorsNote(),
                adventure.getContextAttributes().bump(),
                adventure.getContextAttributes().bumpFrequency(),
                adventure.getModelConfiguration().maxTokenLimit(),
                adventure.getModelConfiguration().temperature(),
                adventure.getModelConfiguration().frequencyPenalty(),
                adventure.getModelConfiguration().presencePenalty(),
                Maps.newHashMap("TKNID", 99D),
                adventure.getModelConfiguration().stopSequences(),
                adventure.getModelConfiguration().stopSequences(),
                Collections.singleton("TKN"),
                Collections.singleton("USRID"),
                Collections.singleton("USRID"),
                Collections.singleton("USRID"),
                Collections.singleton("USRID"),
                adventure.isMultiplayer());
    }

    public static UpdateAdventure sampleWithRequesterId(String requesterId) {

        Adventure adventure = AdventureFixture.privateMultiplayerAdventure().build();
        return new UpdateAdventure(
                AdventureFixture.PUBLIC_ID,
                adventure.getDescription(),
                adventure.getAdventureStart(),
                adventure.getName(),
                WorldFixture.PUBLIC_ID,
                PersonaFixture.PUBLIC_ID,
                adventure.getChannelId(),
                adventure.getVisibility().name(),
                adventure.getModelConfiguration().aiModel().toString(),
                adventure.getModeration().name(),
                requesterId,
                adventure.getGameMode().name(),
                adventure.getContextAttributes().nudge(),
                adventure.getContextAttributes().remember(),
                adventure.getContextAttributes().authorsNote(),
                adventure.getContextAttributes().bump(),
                adventure.getContextAttributes().bumpFrequency(),
                adventure.getModelConfiguration().maxTokenLimit(),
                adventure.getModelConfiguration().temperature(),
                adventure.getModelConfiguration().frequencyPenalty(),
                adventure.getModelConfiguration().presencePenalty(),
                Maps.newHashMap("TKNID", 99D),
                adventure.getModelConfiguration().stopSequences(),
                adventure.getModelConfiguration().stopSequences(),
                Collections.singleton("TKN"),
                Collections.singleton("USRID"),
                Collections.singleton("USRID"),
                Collections.singleton("USRID"),
                Collections.singleton("USRID"),
                adventure.isMultiplayer());
    }

    public static UpdateAdventure sampleWithVisibility(String requesterId, String visibility) {

        Adventure adventure = AdventureFixture.privateMultiplayerAdventure().build();
        return new UpdateAdventure(
                AdventureFixture.PUBLIC_ID,
                adventure.getDescription(),
                adventure.getAdventureStart(),
                adventure.getName(),
                WorldFixture.PUBLIC_ID,
                PersonaFixture.PUBLIC_ID,
                adventure.getChannelId(),
                visibility,
                adventure.getModelConfiguration().aiModel().toString(),
                adventure.getModeration().name(),
                requesterId,
                adventure.getGameMode().name(),
                adventure.getContextAttributes().nudge(),
                adventure.getContextAttributes().remember(),
                adventure.getContextAttributes().authorsNote(),
                adventure.getContextAttributes().bump(),
                adventure.getContextAttributes().bumpFrequency(),
                adventure.getModelConfiguration().maxTokenLimit(),
                adventure.getModelConfiguration().temperature(),
                adventure.getModelConfiguration().frequencyPenalty(),
                adventure.getModelConfiguration().presencePenalty(),
                Maps.newHashMap("TKNID", 99D),
                adventure.getModelConfiguration().stopSequences(),
                adventure.getModelConfiguration().stopSequences(),
                Collections.singleton("TKN"),
                Collections.singleton("USRID"),
                Collections.singleton("USRID"),
                Collections.singleton("USRID"),
                Collections.singleton("USRID"),
                adventure.isMultiplayer());
    }

    public static UpdateAdventure sampleWithMultiplayer(String requesterId, boolean isMultiplayer) {

        Adventure adventure = AdventureFixture.privateMultiplayerAdventure().build();
        return new UpdateAdventure(
                AdventureFixture.PUBLIC_ID,
                adventure.getDescription(),
                adventure.getAdventureStart(),
                adventure.getName(),
                WorldFixture.PUBLIC_ID,
                PersonaFixture.PUBLIC_ID,
                adventure.getChannelId(),
                adventure.getVisibility().name(),
                adventure.getModelConfiguration().aiModel().toString(),
                adventure.getModeration().name(),
                requesterId,
                adventure.getGameMode().name(),
                adventure.getContextAttributes().nudge(),
                adventure.getContextAttributes().remember(),
                adventure.getContextAttributes().authorsNote(),
                adventure.getContextAttributes().bump(),
                adventure.getContextAttributes().bumpFrequency(),
                adventure.getModelConfiguration().maxTokenLimit(),
                adventure.getModelConfiguration().temperature(),
                adventure.getModelConfiguration().frequencyPenalty(),
                adventure.getModelConfiguration().presencePenalty(),
                Maps.newHashMap("TKNID", 99D),
                adventure.getModelConfiguration().stopSequences(),
                adventure.getModelConfiguration().stopSequences(),
                Collections.singleton("TKN"),
                Collections.singleton("USRID"),
                Collections.singleton("USRID"),
                Collections.singleton("USRID"),
                Collections.singleton("USRID"),
                isMultiplayer);
    }

    public static UpdateAdventure sampleWithNullFields(
            UUID adventureId,
            String requesterId,
            String name,
            UUID worldId,
            UUID personaId,
            String channelId,
            String visibility,
            String aiModel,
            String moderation,
            String gameMode,
            Integer maxTokenLimit,
            Double temperature,
            Double frequencyPenalty,
            Double presencePenalty,
            Map<String, Double> logitBiasToAdd,
            Set<String> logitBiasToRemove,
            Set<String> stopSequencesToAdd,
            Set<String> stopSequencesToRemove,
            Set<String> usersAllowedToWriteToAdd,
            Set<String> usersAllowedToWriteToRemove,
            Set<String> usersAllowedToReadToAdd,
            Set<String> usersAllowedToReadToRemove,
            String adventureStart,
            String description,
            String gameModeFull,
            String authorsNote,
            String nudge,
            String remember,
            String bump) {

        return new UpdateAdventure(
                adventureId,
                description,
                adventureStart,
                name,
                worldId,
                personaId,
                channelId,
                visibility,
                aiModel,
                moderation,
                requesterId,
                gameMode,
                nudge,
                remember,
                authorsNote,
                bump,
                null,
                maxTokenLimit,
                temperature,
                frequencyPenalty,
                presencePenalty,
                logitBiasToAdd,
                stopSequencesToAdd,
                stopSequencesToRemove,
                logitBiasToRemove,
                usersAllowedToWriteToAdd,
                usersAllowedToWriteToRemove,
                usersAllowedToReadToAdd,
                usersAllowedToReadToRemove,
                false);
    }
}
