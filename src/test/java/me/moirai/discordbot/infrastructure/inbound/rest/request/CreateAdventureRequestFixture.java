package me.moirai.discordbot.infrastructure.inbound.rest.request;

import me.moirai.discordbot.core.domain.adventure.Adventure;
import me.moirai.discordbot.core.domain.adventure.AdventureFixture;

public class CreateAdventureRequestFixture {

    public static CreateAdventureRequest sample() {

        Adventure adventure = AdventureFixture.privateMultiplayerAdventure().build();
        CreateAdventureRequest request = new CreateAdventureRequest();

        request.setName(adventure.getName());
        request.setPersonaId(adventure.getPersonaId());
        request.setWorldId(adventure.getWorldId());
        request.setDiscordChannelId(adventure.getDiscordChannelId());
        request.setAiModel(adventure.getModelConfiguration().getAiModel().toString());
        request.setLogitBias(adventure.getModelConfiguration().getLogitBias());
        request.setStopSequences(adventure.getModelConfiguration().getStopSequences());
        request.setFrequencyPenalty(adventure.getModelConfiguration().getFrequencyPenalty());
        request.setMaxTokenLimit(adventure.getModelConfiguration().getMaxTokenLimit());
        request.setModeration(adventure.getModeration().name());
        request.setGameMode(adventure.getGameMode().name());
        request.setUsersAllowedToRead(adventure.getUsersAllowedToRead());
        request.setUsersAllowedToWrite(adventure.getUsersAllowedToWrite());
        request.setTemperature(adventure.getModelConfiguration().getTemperature());
        request.setVisibility(adventure.getVisibility().name());
        request.setMultiplayer(false);
        request.setNudge(adventure.getContextAttributes().getNudge());
        request.setAuthorsNote(adventure.getContextAttributes().getAuthorsNote());
        request.setRemember(adventure.getContextAttributes().getRemember());
        request.setBump(adventure.getContextAttributes().getBump());
        request.setBumpFrequency(adventure.getContextAttributes().getBumpFrequency());

        return request;
    }
}
