package me.moirai.storyengine.core.application.usecase.adventure.request;

import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventure;

import java.util.Collections;

import org.assertj.core.util.Maps;

import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;

public class UpdateAdventureFixture {

    public static UpdateAdventure.Builder sample() {

        Adventure adventure = AdventureFixture.privateMultiplayerAdventure().build();
        return UpdateAdventure.builder()
                .id(adventure.getId())
                .name(adventure.getName())
                .description(adventure.getDescription())
                .worldId(String.valueOf(adventure.getWorldId()))
                .personaId(PersonaFixture.PUBLIC_ID)
                .channelId(adventure.getChannelId())
                .visibility(adventure.getVisibility().name())
                .aiModel(adventure.getModelConfiguration().getAiModel().toString())
                .moderation(adventure.getModeration().name())
                .maxTokenLimit(adventure.getModelConfiguration().getMaxTokenLimit())
                .temperature(adventure.getModelConfiguration().getTemperature())
                .frequencyPenalty(adventure.getModelConfiguration().getFrequencyPenalty())
                .presencePenalty(adventure.getModelConfiguration().getPresencePenalty())
                .stopSequencesToAdd(adventure.getModelConfiguration().getStopSequences())
                .stopSequencesToRemove(adventure.getModelConfiguration().getStopSequences())
                .logitBiasToAdd(Maps.newHashMap("TKNID", 99D))
                .logitBiasToRemove(Collections.singleton("TKN"))
                .usersAllowedToWriteToAdd(Collections.singleton("USRID"))
                .usersAllowedToWriteToRemove(Collections.singleton("USRID"))
                .usersAllowedToReadToAdd(Collections.singleton("USRID"))
                .usersAllowedToReadToRemove(Collections.singleton("USRID"))
                .gameMode(adventure.getGameMode().name())
                .requesterId(adventure.getOwnerId())
                .nudge(adventure.getContextAttributes().getNudge())
                .remember(adventure.getContextAttributes().getRemember())
                .authorsNote(adventure.getContextAttributes().getAuthorsNote())
                .bump(adventure.getContextAttributes().getBump())
                .bumpFrequency(adventure.getContextAttributes().getBumpFrequency())
                .isMultiplayer(adventure.isMultiplayer())
                .adventureStart(adventure.getAdventureStart());
    }
}
