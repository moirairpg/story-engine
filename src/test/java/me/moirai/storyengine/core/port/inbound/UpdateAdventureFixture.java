package me.moirai.storyengine.core.port.inbound;

import java.util.List;
import java.util.Set;

import me.moirai.storyengine.common.enums.Visibility;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.port.inbound.adventure.ContextAttributesDto;
import me.moirai.storyengine.core.port.inbound.adventure.ModelConfigurationDto;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventure;

public class UpdateAdventureFixture {

    public static UpdateAdventure sample() {

        var adventure = AdventureFixture.privateMultiplayerAdventure().build();
        return new UpdateAdventure(
                AdventureFixture.PUBLIC_ID,
                adventure.getName(),
                adventure.getDescription(),
                adventure.getAdventureStart(),
                "Aria",
                "A helpful guide",
                adventure.getVisibility(),
                adventure.getModeration(),
                adventure.isMultiplayer(),
                Set.of(),
                new ModelConfigurationDto(
                        adventure.getModelConfiguration().getAiModel(),
                        adventure.getModelConfiguration().getMaxTokenLimit(),
                        adventure.getModelConfiguration().getTemperature()),
                new ContextAttributesDto(
                        adventure.getContextAttributes().nudge(),
                        adventure.getContextAttributes().authorsNote(),
                        adventure.getContextAttributes().scene(),
                        adventure.getContextAttributes().bump(),
                        adventure.getContextAttributes().bumpFrequency()),
                List.of(),
                List.of(),
                List.of());
    }

    public static UpdateAdventure sampleWithRequesterId(String requesterId) {

        Adventure adventure = AdventureFixture.privateMultiplayerAdventure().build();
        return new UpdateAdventure(
                AdventureFixture.PUBLIC_ID,
                adventure.getName(),
                adventure.getDescription(),
                adventure.getAdventureStart(),
                "Aria",
                "A helpful guide",
                adventure.getVisibility(),
                adventure.getModeration(),
                adventure.isMultiplayer(),
                Set.of(),
                new ModelConfigurationDto(
                        adventure.getModelConfiguration().getAiModel(),
                        adventure.getModelConfiguration().getMaxTokenLimit(),
                        adventure.getModelConfiguration().getTemperature()),
                new ContextAttributesDto(
                        adventure.getContextAttributes().nudge(),
                        adventure.getContextAttributes().authorsNote(),
                        adventure.getContextAttributes().scene(),
                        adventure.getContextAttributes().bump(),
                        adventure.getContextAttributes().bumpFrequency()),
                List.of(),
                List.of(),
                List.of());
    }

    public static UpdateAdventure sampleWithVisibility(String requesterId, Visibility visibility) {

        Adventure adventure = AdventureFixture.privateMultiplayerAdventure().build();
        return new UpdateAdventure(
                AdventureFixture.PUBLIC_ID,
                adventure.getName(),
                adventure.getDescription(),
                adventure.getAdventureStart(),
                "Aria",
                "A helpful guide",
                visibility,
                adventure.getModeration(),
                adventure.isMultiplayer(),
                Set.of(),
                new ModelConfigurationDto(
                        adventure.getModelConfiguration().getAiModel(),
                        adventure.getModelConfiguration().getMaxTokenLimit(),
                        adventure.getModelConfiguration().getTemperature()),
                new ContextAttributesDto(
                        adventure.getContextAttributes().nudge(),
                        adventure.getContextAttributes().authorsNote(),
                        adventure.getContextAttributes().scene(),
                        adventure.getContextAttributes().bump(),
                        adventure.getContextAttributes().bumpFrequency()),
                List.of(),
                List.of(),
                List.of());
    }

    public static UpdateAdventure sampleWithMultiplayer(String requesterId, boolean isMultiplayer) {

        Adventure adventure = AdventureFixture.privateMultiplayerAdventure().build();
        return new UpdateAdventure(
                AdventureFixture.PUBLIC_ID,
                adventure.getName(),
                adventure.getDescription(),
                adventure.getAdventureStart(),
                "Aria",
                "A helpful guide",
                adventure.getVisibility(),
                adventure.getModeration(),
                isMultiplayer,
                Set.of(),
                new ModelConfigurationDto(
                        adventure.getModelConfiguration().getAiModel(),
                        adventure.getModelConfiguration().getMaxTokenLimit(),
                        adventure.getModelConfiguration().getTemperature()),
                new ContextAttributesDto(
                        adventure.getContextAttributes().nudge(),
                        adventure.getContextAttributes().authorsNote(),
                        adventure.getContextAttributes().scene(),
                        adventure.getContextAttributes().bump(),
                        adventure.getContextAttributes().bumpFrequency()),
                List.of(),
                List.of(),
                List.of());
    }
}
