package me.moirai.storyengine.core.port.inbound;

import me.moirai.storyengine.core.port.inbound.adventure.CreateAdventure;

import static me.moirai.storyengine.common.enums.Moderation.STRICT;
import static me.moirai.storyengine.common.enums.Visibility.PRIVATE;

import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.domain.world.WorldFixture;

public class CreateAdventureFixture {

    public static CreateAdventure sample() {

        Adventure adventure = AdventureFixture.privateMultiplayerAdventure().build();
        return new CreateAdventure(
                adventure.getName(),
                adventure.getDescription(),
                WorldFixture.PUBLIC_ID,
                PersonaFixture.PUBLIC_ID,
                adventure.getChannelId(),
                PRIVATE,
                adventure.getModelConfiguration().getAiModel(),
                STRICT,
                "RQSTRID",
                adventure.getGameMode(),
                adventure.getContextAttributes().nudge(),
                adventure.getContextAttributes().remember(),
                adventure.getContextAttributes().authorsNote(),
                adventure.getContextAttributes().bump(),
                adventure.getContextAttributes().bumpFrequency(),
                adventure.getModelConfiguration().getMaxTokenLimit(),
                1.7,
                adventure.getModelConfiguration().getFrequencyPenalty(),
                adventure.getModelConfiguration().getPresencePenalty(),
                adventure.getModelConfiguration().getLogitBias(),
                adventure.getModelConfiguration().getStopSequences(),
                adventure.getUsersAllowedToWrite(),
                adventure.getUsersAllowedToRead(),
                adventure.isMultiplayer());
    }

    public static CreateAdventure sampleWithRequesterId(String requesterId) {

        Adventure adventure = AdventureFixture.privateMultiplayerAdventure().build();
        return new CreateAdventure(
                adventure.getName(),
                adventure.getDescription(),
                WorldFixture.PUBLIC_ID,
                PersonaFixture.PUBLIC_ID,
                adventure.getChannelId(),
                PRIVATE,
                adventure.getModelConfiguration().getAiModel(),
                STRICT,
                requesterId,
                adventure.getGameMode(),
                adventure.getContextAttributes().nudge(),
                adventure.getContextAttributes().remember(),
                adventure.getContextAttributes().authorsNote(),
                adventure.getContextAttributes().bump(),
                adventure.getContextAttributes().bumpFrequency(),
                adventure.getModelConfiguration().getMaxTokenLimit(),
                1.7,
                adventure.getModelConfiguration().getFrequencyPenalty(),
                adventure.getModelConfiguration().getPresencePenalty(),
                adventure.getModelConfiguration().getLogitBias(),
                adventure.getModelConfiguration().getStopSequences(),
                adventure.getUsersAllowedToWrite(),
                adventure.getUsersAllowedToRead(),
                adventure.isMultiplayer());
    }
}
