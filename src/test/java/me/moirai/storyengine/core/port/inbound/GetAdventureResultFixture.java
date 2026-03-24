package me.moirai.storyengine.core.port.inbound;

import me.moirai.storyengine.core.port.inbound.adventure.AdventureDetails;

import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.port.inbound.adventure.ContextAttributesDto;
import me.moirai.storyengine.core.port.inbound.adventure.ModelConfigurationDto;

public class GetAdventureResultFixture {

    public static AdventureDetails privateMultiplayerAdventure() {

        // given
        Adventure adventure = AdventureFixture.privateMultiplayerAdventure().build();

        var modelConfiguration = new ModelConfigurationDto(
                adventure.getModelConfiguration().getAiModel(),
                adventure.getModelConfiguration().getMaxTokenLimit(),
                adventure.getModelConfiguration().getTemperature(),
                adventure.getModelConfiguration().getFrequencyPenalty(),
                adventure.getModelConfiguration().getPresencePenalty(),
                adventure.getModelConfiguration().getStopSequences(),
                adventure.getModelConfiguration().getLogitBias());

        var contextAttributes = new ContextAttributesDto(
                adventure.getContextAttributes().nudge(),
                adventure.getContextAttributes().authorsNote(),
                adventure.getContextAttributes().remember(),
                adventure.getContextAttributes().bump(),
                adventure.getContextAttributes().bumpFrequency());

        return new AdventureDetails(
                adventure.getPublicId(),
                adventure.getName(),
                adventure.getDescription(),
                adventure.getAdventureStart(),
                WorldFixture.PUBLIC_ID,
                PersonaFixture.PUBLIC_ID,
                adventure.getChannelId(),
                adventure.getVisibility().name(),
                adventure.getModeration().name(),
                adventure.getGameMode().name(),
                adventure.getOwnerId(),
                adventure.isMultiplayer(),
                adventure.getCreationDate(),
                adventure.getLastUpdateDate(),
                modelConfiguration,
                contextAttributes,
                adventure.getUsersAllowedToRead(),
                adventure.getUsersAllowedToWrite());
    }

    public static AdventureDetails publicMultiplayerAdventure() {

        // given
        Adventure adventure = AdventureFixture.publicMultiplayerAdventure().build();

        var modelConfiguration = new ModelConfigurationDto(
                adventure.getModelConfiguration().getAiModel(),
                adventure.getModelConfiguration().getMaxTokenLimit(),
                adventure.getModelConfiguration().getTemperature(),
                adventure.getModelConfiguration().getFrequencyPenalty(),
                adventure.getModelConfiguration().getPresencePenalty(),
                adventure.getModelConfiguration().getStopSequences(),
                adventure.getModelConfiguration().getLogitBias());

        var contextAttributes = new ContextAttributesDto(
                adventure.getContextAttributes().nudge(),
                adventure.getContextAttributes().authorsNote(),
                adventure.getContextAttributes().remember(),
                adventure.getContextAttributes().bump(),
                adventure.getContextAttributes().bumpFrequency());

        return new AdventureDetails(
                adventure.getPublicId(),
                adventure.getName(),
                adventure.getDescription(),
                adventure.getAdventureStart(),
                WorldFixture.PUBLIC_ID,
                PersonaFixture.PUBLIC_ID,
                adventure.getChannelId(),
                adventure.getVisibility().name(),
                adventure.getModeration().name(),
                adventure.getGameMode().name(),
                adventure.getOwnerId(),
                adventure.isMultiplayer(),
                adventure.getCreationDate(),
                adventure.getLastUpdateDate(),
                modelConfiguration,
                contextAttributes,
                adventure.getUsersAllowedToRead(),
                adventure.getUsersAllowedToWrite());
    }
}
