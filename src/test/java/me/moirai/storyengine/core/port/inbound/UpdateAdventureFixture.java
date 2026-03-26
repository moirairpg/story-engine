package me.moirai.storyengine.core.port.inbound;

import java.util.Set;

import org.assertj.core.util.Maps;

import me.moirai.storyengine.common.enums.Visibility;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventure;

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
                adventure.getModelConfiguration().getAiModel(),
                adventure.getModeration(),
                adventure.getContextAttributes().nudge(),
                adventure.getContextAttributes().scene(),
                adventure.getContextAttributes().authorsNote(),
                adventure.getContextAttributes().bump(),
                adventure.getContextAttributes().bumpFrequency(),
                adventure.getModelConfiguration().getMaxTokenLimit(),
                adventure.getModelConfiguration().getTemperature(),
                adventure.getModelConfiguration().getFrequencyPenalty(),
                adventure.getModelConfiguration().getPresencePenalty(),
                Maps.newHashMap("TKNID", 99D),
                adventure.getModelConfiguration().getStopSequences(),
                adventure.getModelConfiguration().getStopSequences(),
                Set.of("TKN"),
                Set.of(12345L),
                Set.of(12345L),
                Set.of(12345L),
                Set.of(12345L),
                adventure.isMultiplayer());
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
                adventure.getModelConfiguration().getAiModel(),
                adventure.getModeration(),
                adventure.getContextAttributes().nudge(),
                adventure.getContextAttributes().scene(),
                adventure.getContextAttributes().authorsNote(),
                adventure.getContextAttributes().bump(),
                adventure.getContextAttributes().bumpFrequency(),
                adventure.getModelConfiguration().getMaxTokenLimit(),
                adventure.getModelConfiguration().getTemperature(),
                adventure.getModelConfiguration().getFrequencyPenalty(),
                adventure.getModelConfiguration().getPresencePenalty(),
                Maps.newHashMap("TKNID", 99D),
                adventure.getModelConfiguration().getStopSequences(),
                adventure.getModelConfiguration().getStopSequences(),
                Set.of("TKN"),
                Set.of(12345L),
                Set.of(12345L),
                Set.of(12345L),
                Set.of(12345L),
                adventure.isMultiplayer());
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
                adventure.getModelConfiguration().getAiModel(),
                adventure.getModeration(),
                adventure.getContextAttributes().nudge(),
                adventure.getContextAttributes().scene(),
                adventure.getContextAttributes().authorsNote(),
                adventure.getContextAttributes().bump(),
                adventure.getContextAttributes().bumpFrequency(),
                adventure.getModelConfiguration().getMaxTokenLimit(),
                adventure.getModelConfiguration().getTemperature(),
                adventure.getModelConfiguration().getFrequencyPenalty(),
                adventure.getModelConfiguration().getPresencePenalty(),
                Maps.newHashMap("TKNID", 99D),
                adventure.getModelConfiguration().getStopSequences(),
                adventure.getModelConfiguration().getStopSequences(),
                Set.of("TKN"),
                Set.of(12345L),
                Set.of(12345L),
                Set.of(12345L),
                Set.of(12345L),
                adventure.isMultiplayer());
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
                adventure.getModelConfiguration().getAiModel(),
                adventure.getModeration(),
                adventure.getContextAttributes().nudge(),
                adventure.getContextAttributes().scene(),
                adventure.getContextAttributes().authorsNote(),
                adventure.getContextAttributes().bump(),
                adventure.getContextAttributes().bumpFrequency(),
                adventure.getModelConfiguration().getMaxTokenLimit(),
                adventure.getModelConfiguration().getTemperature(),
                adventure.getModelConfiguration().getFrequencyPenalty(),
                adventure.getModelConfiguration().getPresencePenalty(),
                Maps.newHashMap("TKNID", 99D),
                adventure.getModelConfiguration().getStopSequences(),
                adventure.getModelConfiguration().getStopSequences(),
                Set.of("TKN"),
                Set.of(12345L),
                Set.of(12345L),
                Set.of(12345L),
                Set.of(12345L),
                isMultiplayer);
    }
}
