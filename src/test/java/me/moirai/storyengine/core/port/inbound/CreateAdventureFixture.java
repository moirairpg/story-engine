package me.moirai.storyengine.core.port.inbound;

import static me.moirai.storyengine.common.enums.Moderation.STRICT;
import static me.moirai.storyengine.common.enums.Visibility.PRIVATE;

import java.util.Set;

import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.port.inbound.adventure.ContextAttributesDto;
import me.moirai.storyengine.core.port.inbound.adventure.CreateAdventure;
import me.moirai.storyengine.core.port.inbound.adventure.ModelConfigurationDto;

public class CreateAdventureFixture {

    public static CreateAdventure sample() {

        var adventure = AdventureFixture.privateMultiplayerAdventure().build();
        return new CreateAdventure(
                adventure.getName(),
                adventure.getDescription(),
                WorldFixture.PUBLIC_ID,
                "Aria",
                "A helpful guide",
                PRIVATE,
                STRICT,
                adventure.isMultiplayer(),
                adventure.getAdventureStart(),
                Set.of(),
                Set.of(),
                new ModelConfigurationDto(
                        adventure.getModelConfiguration().getAiModel(),
                        adventure.getModelConfiguration().getMaxTokenLimit(),
                        1.7),
                new ContextAttributesDto(
                        adventure.getContextAttributes().nudge(),
                        adventure.getContextAttributes().authorsNote(),
                        adventure.getContextAttributes().scene(),
                        adventure.getContextAttributes().bump(),
                        adventure.getContextAttributes().bumpFrequency()));
    }

    public static CreateAdventure sampleWithRequesterId(String requesterId) {

        var adventure = AdventureFixture.privateMultiplayerAdventure().build();
        return new CreateAdventure(
                adventure.getName(),
                adventure.getDescription(),
                WorldFixture.PUBLIC_ID,
                "Aria",
                "A helpful guide",
                PRIVATE,
                STRICT,
                adventure.isMultiplayer(),
                adventure.getAdventureStart(),
                Set.of(),
                Set.of(),
                new ModelConfigurationDto(
                        adventure.getModelConfiguration().getAiModel(),
                        adventure.getModelConfiguration().getMaxTokenLimit(),
                        1.7),
                new ContextAttributesDto(
                        adventure.getContextAttributes().nudge(),
                        adventure.getContextAttributes().authorsNote(),
                        adventure.getContextAttributes().scene(),
                        adventure.getContextAttributes().bump(),
                        adventure.getContextAttributes().bumpFrequency()));
    }
}
