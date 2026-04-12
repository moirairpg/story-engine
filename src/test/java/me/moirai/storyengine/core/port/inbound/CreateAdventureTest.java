package me.moirai.storyengine.core.port.inbound;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;

import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.port.inbound.adventure.ContextAttributesDto;
import me.moirai.storyengine.core.port.inbound.adventure.CreateAdventure;
import me.moirai.storyengine.core.port.inbound.adventure.ModelConfigurationDto;

public class CreateAdventureTest {

    @Test
    public void createAdventure_whenValidDate_thenInstanceIsCreated() {

        // given
        Adventure adventure = AdventureFixture.privateMultiplayerAdventure().build();

        // when
        var createAdventure = new CreateAdventure(
                adventure.getName(),
                adventure.getDescription(),
                WorldFixture.PUBLIC_ID,
                adventure.getNarratorName(),
                adventure.getNarratorPersonality(),
                adventure.getVisibility(),
                adventure.getModeration(),
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

        // then
        assertThat(createAdventure.description()).isEqualTo(adventure.getDescription());
        assertThat(createAdventure.name()).isEqualTo(adventure.getName());
        assertThat(createAdventure.narratorName()).isEqualTo(adventure.getNarratorName());
        assertThat(createAdventure.worldId()).isEqualTo(WorldFixture.PUBLIC_ID);
        assertThat(createAdventure.visibility()).isEqualTo(adventure.getVisibility());
        assertThat(createAdventure.modelConfiguration().temperature()).isEqualTo(1.7);
        assertThat(createAdventure.modelConfiguration().maxTokenLimit()).isEqualTo(adventure.getModelConfiguration().getMaxTokenLimit());
        assertThat(createAdventure.contextAttributes().scene()).isEqualTo(adventure.getContextAttributes().scene());
        assertThat(createAdventure.contextAttributes().authorsNote()).isEqualTo(adventure.getContextAttributes().authorsNote());
        assertThat(createAdventure.contextAttributes().nudge()).isEqualTo(adventure.getContextAttributes().nudge());
        assertThat(createAdventure.contextAttributes().bump()).isEqualTo(adventure.getContextAttributes().bump());
        assertThat(createAdventure.contextAttributes().bumpFrequency()).isEqualTo(adventure.getContextAttributes().bumpFrequency());
        assertThat(createAdventure.modelConfiguration().aiModel()).isEqualTo(adventure.getModelConfiguration().getAiModel());
    }

    @Test
    public void createAdventure_whenModelConfigurationIsNull_thenModelConfigurationIsNull() {

        // given
        var sample = CreateAdventureFixture.sample();

        // when
        var createAdventure = new CreateAdventure(
                sample.name(),
                sample.description(),
                sample.worldId(),
                sample.narratorName(),
                sample.narratorPersonality(),
                sample.visibility(),
                sample.moderation(),
                sample.isMultiplayer(),
                sample.adventureStart(),
                Set.of(),
                sample.permissions(),
                null,
                sample.contextAttributes());

        // then
        assertThat(createAdventure.modelConfiguration()).isNull();
    }

    @Test
    public void createAdventure_whenPermissionsIsNull_thenSetIsEmpty() {

        // given
        var sample = CreateAdventureFixture.sample();

        // when
        var createAdventure = new CreateAdventure(
                sample.name(),
                sample.description(),
                sample.worldId(),
                sample.narratorName(),
                sample.narratorPersonality(),
                sample.visibility(),
                sample.moderation(),
                sample.isMultiplayer(),
                sample.adventureStart(),
                Set.of(),
                null,
                sample.modelConfiguration(),
                sample.contextAttributes());

        // then
        assertThat(createAdventure.permissions()).isEmpty();
    }
}
