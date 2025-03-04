package me.moirai.discordbot.infrastructure.inbound.rest.response;

import me.moirai.discordbot.core.domain.adventure.Adventure;
import me.moirai.discordbot.core.domain.adventure.AdventureFixture;

public class AdventureResponseFixture {

    public static AdventureResponse.Builder sample() {

        Adventure adventure = AdventureFixture.privateMultiplayerAdventure().build();
        return AdventureResponse.builder()
                .id(adventure.getId())
                .name(adventure.getName())
                .personaId(adventure.getPersonaId())
                .worldId(adventure.getWorldId())
                .discordChannelId(adventure.getDiscordChannelId())
                .aiModel(adventure.getModelConfiguration().getAiModel().toString())
                .logitBias(adventure.getModelConfiguration().getLogitBias())
                .stopSequences(adventure.getModelConfiguration().getStopSequences())
                .maxTokenLimit(adventure.getModelConfiguration().getMaxTokenLimit())
                .presencePenalty(adventure.getModelConfiguration().getPresencePenalty())
                .frequencyPenalty(adventure.getModelConfiguration().getFrequencyPenalty())
                .moderation(adventure.getModeration().name())
                .usersAllowedToRead(adventure.getUsersAllowedToRead())
                .usersAllowedToWrite(adventure.getUsersAllowedToWrite())
                .temperature(adventure.getModelConfiguration().getTemperature())
                .visibility(adventure.getVisibility().name())
                .gameMode(adventure.getGameMode().name())
                .nudge(adventure.getContextAttributes().getNudge())
                .authorsNote(adventure.getContextAttributes().getAuthorsNote())
                .remember(adventure.getContextAttributes().getRemember())
                .bump(adventure.getContextAttributes().getBump())
                .bumpFrequency(adventure.getContextAttributes().getBumpFrequency())
                .ownerDiscordId(adventure.getOwnerDiscordId())
                .isMultiplayer(adventure.isMultiplayer())
                .creationDate(adventure.getCreationDate())
                .lastUpdateDate(adventure.getLastUpdateDate());
    }
}