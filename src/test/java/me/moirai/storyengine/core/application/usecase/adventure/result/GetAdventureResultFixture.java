package me.moirai.storyengine.core.application.usecase.adventure.result;

import me.moirai.storyengine.core.port.inbound.adventure.AdventureDetails;

import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;

public class GetAdventureResultFixture {

    public static AdventureDetails.Builder privateMultiplayerAdventure() {

        Adventure adventure = AdventureFixture.privateMultiplayerAdventure().build();
        return AdventureDetails.builder()
                .id(adventure.getId())
                .name(adventure.getName())
                .personaId(adventure.getPersonaId())
                .worldId(adventure.getWorldId())
                .channelId(adventure.getChannelId())
                .aiModel(adventure.getModelConfiguration().getAiModel().toString())
                .logitBias(adventure.getModelConfiguration().getLogitBias())
                .stopSequences(adventure.getModelConfiguration().getStopSequences())
                .frequencyPenalty(adventure.getModelConfiguration().getFrequencyPenalty())
                .maxTokenLimit(adventure.getModelConfiguration().getMaxTokenLimit())
                .moderation(adventure.getModeration().name())
                .gameMode(adventure.getGameMode().name())
                .temperature(adventure.getModelConfiguration().getTemperature())
                .visibility(adventure.getVisibility().name())
                .ownerId(adventure.getOwnerId())
                .usersAllowedToRead(adventure.getUsersAllowedToRead())
                .usersAllowedToWrite(adventure.getUsersAllowedToWrite());
    }
    public static AdventureDetails.Builder publicMultiplayerAdventure() {

        Adventure adventure = AdventureFixture.publicMultiplayerAdventure().build();
        return AdventureDetails.builder()
                .id(adventure.getId())
                .name(adventure.getName())
                .personaId(adventure.getPersonaId())
                .worldId(adventure.getWorldId())
                .channelId(adventure.getChannelId())
                .aiModel(adventure.getModelConfiguration().getAiModel().toString())
                .logitBias(adventure.getModelConfiguration().getLogitBias())
                .stopSequences(adventure.getModelConfiguration().getStopSequences())
                .frequencyPenalty(adventure.getModelConfiguration().getFrequencyPenalty())
                .maxTokenLimit(adventure.getModelConfiguration().getMaxTokenLimit())
                .moderation(adventure.getModeration().name())
                .gameMode(adventure.getGameMode().name())
                .temperature(adventure.getModelConfiguration().getTemperature())
                .visibility(adventure.getVisibility().name())
                .ownerId(adventure.getOwnerId())
                .usersAllowedToRead(adventure.getUsersAllowedToRead())
                .usersAllowedToWrite(adventure.getUsersAllowedToWrite());
    }
}
