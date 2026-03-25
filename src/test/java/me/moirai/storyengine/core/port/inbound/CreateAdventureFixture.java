package me.moirai.storyengine.core.port.inbound;

import static me.moirai.storyengine.common.enums.Moderation.STRICT;
import static me.moirai.storyengine.common.enums.Visibility.PRIVATE;

import java.util.Set;

import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.port.inbound.adventure.CreateAdventure;

public class CreateAdventureFixture {

    public static CreateAdventure sample() {

        var adventure = AdventureFixture.privateMultiplayerAdventure().build();
        return new CreateAdventure(
                adventure.getName(),
                adventure.getDescription(),
                WorldFixture.PUBLIC_ID,
                PersonaFixture.PUBLIC_ID,
                adventure.getChannelId(),
                PRIVATE,
                adventure.getModelConfiguration().getAiModel(),
                STRICT,
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
                Set.of(),
                Set.of(),
                adventure.isMultiplayer());
    }

    public static CreateAdventure sampleWithRequesterId(String requesterId) {

        var adventure = AdventureFixture.privateMultiplayerAdventure().build();
        return new CreateAdventure(
                adventure.getName(),
                adventure.getDescription(),
                WorldFixture.PUBLIC_ID,
                PersonaFixture.PUBLIC_ID,
                adventure.getChannelId(),
                PRIVATE,
                adventure.getModelConfiguration().getAiModel(),
                STRICT,
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
                Set.of(),
                Set.of(),
                adventure.isMultiplayer());
    }
}
