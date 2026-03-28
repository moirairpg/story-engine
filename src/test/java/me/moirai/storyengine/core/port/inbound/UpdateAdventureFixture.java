package me.moirai.storyengine.core.port.inbound;

import java.util.Set;

import org.assertj.core.util.Maps;

import me.moirai.storyengine.common.enums.Visibility;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.port.inbound.adventure.ContextAttributesDto;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventure;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateModelConfigurationDto;

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
                adventure.getVisibility(),
                adventure.getModeration(),
                adventure.isMultiplayer(),
                Set.of(),
                new UpdateModelConfigurationDto(
                        adventure.getModelConfiguration().getAiModel(),
                        adventure.getModelConfiguration().getMaxTokenLimit(),
                        adventure.getModelConfiguration().getTemperature(),
                        adventure.getModelConfiguration().getFrequencyPenalty(),
                        adventure.getModelConfiguration().getPresencePenalty(),
                        adventure.getModelConfiguration().getStopSequences(),
                        adventure.getModelConfiguration().getStopSequences(),
                        Maps.newHashMap("TKNID", 99D),
                        Set.of("TKN")),
                new ContextAttributesDto(
                        adventure.getContextAttributes().nudge(),
                        adventure.getContextAttributes().authorsNote(),
                        adventure.getContextAttributes().scene(),
                        adventure.getContextAttributes().bump(),
                        adventure.getContextAttributes().bumpFrequency()));
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
                adventure.getVisibility(),
                adventure.getModeration(),
                adventure.isMultiplayer(),
                Set.of(),
                new UpdateModelConfigurationDto(
                        adventure.getModelConfiguration().getAiModel(),
                        adventure.getModelConfiguration().getMaxTokenLimit(),
                        adventure.getModelConfiguration().getTemperature(),
                        adventure.getModelConfiguration().getFrequencyPenalty(),
                        adventure.getModelConfiguration().getPresencePenalty(),
                        adventure.getModelConfiguration().getStopSequences(),
                        adventure.getModelConfiguration().getStopSequences(),
                        Maps.newHashMap("TKNID", 99D),
                        Set.of("TKN")),
                new ContextAttributesDto(
                        adventure.getContextAttributes().nudge(),
                        adventure.getContextAttributes().authorsNote(),
                        adventure.getContextAttributes().scene(),
                        adventure.getContextAttributes().bump(),
                        adventure.getContextAttributes().bumpFrequency()));
    }

    public static UpdateAdventure sampleWithVisibility(String requesterId, Visibility visibility) {

        Adventure adventure = AdventureFixture.privateMultiplayerAdventure().build();
        return new UpdateAdventure(
                AdventureFixture.PUBLIC_ID,
                adventure.getDescription(),
                adventure.getAdventureStart(),
                adventure.getName(),
                WorldFixture.PUBLIC_ID,
                PersonaFixture.PUBLIC_ID,
                visibility,
                adventure.getModeration(),
                adventure.isMultiplayer(),
                Set.of(),
                new UpdateModelConfigurationDto(
                        adventure.getModelConfiguration().getAiModel(),
                        adventure.getModelConfiguration().getMaxTokenLimit(),
                        adventure.getModelConfiguration().getTemperature(),
                        adventure.getModelConfiguration().getFrequencyPenalty(),
                        adventure.getModelConfiguration().getPresencePenalty(),
                        adventure.getModelConfiguration().getStopSequences(),
                        adventure.getModelConfiguration().getStopSequences(),
                        Maps.newHashMap("TKNID", 99D),
                        Set.of("TKN")),
                new ContextAttributesDto(
                        adventure.getContextAttributes().nudge(),
                        adventure.getContextAttributes().authorsNote(),
                        adventure.getContextAttributes().scene(),
                        adventure.getContextAttributes().bump(),
                        adventure.getContextAttributes().bumpFrequency()));
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
                adventure.getVisibility(),
                adventure.getModeration(),
                isMultiplayer,
                Set.of(),
                new UpdateModelConfigurationDto(
                        adventure.getModelConfiguration().getAiModel(),
                        adventure.getModelConfiguration().getMaxTokenLimit(),
                        adventure.getModelConfiguration().getTemperature(),
                        adventure.getModelConfiguration().getFrequencyPenalty(),
                        adventure.getModelConfiguration().getPresencePenalty(),
                        adventure.getModelConfiguration().getStopSequences(),
                        adventure.getModelConfiguration().getStopSequences(),
                        Maps.newHashMap("TKNID", 99D),
                        Set.of("TKN")),
                new ContextAttributesDto(
                        adventure.getContextAttributes().nudge(),
                        adventure.getContextAttributes().authorsNote(),
                        adventure.getContextAttributes().scene(),
                        adventure.getContextAttributes().bump(),
                        adventure.getContextAttributes().bumpFrequency()));
    }
}
