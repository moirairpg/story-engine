package me.moirai.storyengine.core.port.inbound;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.assertj.core.util.Maps;
import org.junit.jupiter.api.Test;

import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.port.inbound.adventure.ContextAttributesDto;
import me.moirai.storyengine.core.port.inbound.adventure.CreateAdventure;
import me.moirai.storyengine.core.port.inbound.adventure.ModelConfigurationDto;

public class CreateAdventureTest {

    @Test
    public void updateAdventure_whenValidDate_thenInstanceIsCreated() {

        // Given
        Adventure adventure = AdventureFixture.privateMultiplayerAdventure().build();

        // When
        CreateAdventure updateAdventure = new CreateAdventure(
                adventure.getName(),
                adventure.getDescription(),
                WorldFixture.PUBLIC_ID,
                PersonaFixture.PUBLIC_ID,
                adventure.getVisibility(),
                adventure.getModeration(),
                adventure.isMultiplayer(),
                Set.of(),
                new ModelConfigurationDto(
                        adventure.getModelConfiguration().getAiModel(),
                        adventure.getModelConfiguration().getMaxTokenLimit(),
                        1.7,
                        adventure.getModelConfiguration().getFrequencyPenalty(),
                        adventure.getModelConfiguration().getPresencePenalty(),
                        adventure.getModelConfiguration().getStopSequences(),
                        Maps.newHashMap("TKNID", 99D)),
                new ContextAttributesDto(
                        adventure.getContextAttributes().nudge(),
                        adventure.getContextAttributes().authorsNote(),
                        adventure.getContextAttributes().scene(),
                        adventure.getContextAttributes().bump(),
                        adventure.getContextAttributes().bumpFrequency()));

        // Then
        assertThat(updateAdventure.description()).isEqualTo(adventure.getDescription());
        assertThat(updateAdventure.name()).isEqualTo(adventure.getName());
        assertThat(updateAdventure.personaId()).isEqualTo(PersonaFixture.PUBLIC_ID);
        assertThat(updateAdventure.worldId()).isEqualTo(WorldFixture.PUBLIC_ID);
        assertThat(updateAdventure.visibility()).isEqualTo(adventure.getVisibility());
        assertThat(updateAdventure.modelConfiguration().presencePenalty()).isEqualTo(adventure.getModelConfiguration().getPresencePenalty());
        assertThat(updateAdventure.modelConfiguration().frequencyPenalty()).isEqualTo(adventure.getModelConfiguration().getFrequencyPenalty());
        assertThat(updateAdventure.modelConfiguration().temperature()).isEqualTo(1.7);
        assertThat(updateAdventure.modelConfiguration().maxTokenLimit()).isEqualTo(adventure.getModelConfiguration().getMaxTokenLimit());
        assertThat(updateAdventure.contextAttributes().scene()).isEqualTo(adventure.getContextAttributes().scene());
        assertThat(updateAdventure.contextAttributes().authorsNote()).isEqualTo(adventure.getContextAttributes().authorsNote());
        assertThat(updateAdventure.contextAttributes().nudge()).isEqualTo(adventure.getContextAttributes().nudge());
        assertThat(updateAdventure.contextAttributes().bump()).isEqualTo(adventure.getContextAttributes().bump());
        assertThat(updateAdventure.contextAttributes().bumpFrequency()).isEqualTo(adventure.getContextAttributes().bumpFrequency());
        assertThat(updateAdventure.modelConfiguration().aiModel()).isEqualTo(adventure.getModelConfiguration().getAiModel());
    }

    @Test
    public void updateAdventure_whenStopSequencesIsNull_thenListIsEmpty() {

        // Given
        CreateAdventure sample = CreateAdventureFixture.sample();

        // When
        CreateAdventure updateAdventure = new CreateAdventure(
                sample.name(),
                sample.description(),
                sample.worldId(),
                sample.personaId(),
                sample.visibility(),
                sample.moderation(),
                sample.isMultiplayer(),
                sample.permissions(),
                null,
                sample.contextAttributes());

        // Then
        assertThat(updateAdventure.modelConfiguration()).isNull();
    }

    @Test
    public void updateAdventure_whenLogitBiasIsNull_thenListIsEmpty() {

        // Given
        CreateAdventure sample = CreateAdventureFixture.sample();

        // When
        CreateAdventure updateAdventure = new CreateAdventure(
                sample.name(),
                sample.description(),
                sample.worldId(),
                sample.personaId(),
                sample.visibility(),
                sample.moderation(),
                sample.isMultiplayer(),
                sample.permissions(),
                null,
                sample.contextAttributes());

        // Then
        assertThat(updateAdventure.modelConfiguration()).isNull();
    }

    @Test
    public void updateAdventure_whenPermissionsIsNull_thenSetIsEmpty() {

        // Given
        CreateAdventure sample = CreateAdventureFixture.sample();

        // When
        CreateAdventure updateAdventure = new CreateAdventure(
                sample.name(),
                sample.description(),
                sample.worldId(),
                sample.personaId(),
                sample.visibility(),
                sample.moderation(),
                sample.isMultiplayer(),
                null,
                sample.modelConfiguration(),
                sample.contextAttributes());

        // Then
        assertThat(updateAdventure.permissions()).isEmpty();
    }
}
