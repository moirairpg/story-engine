package me.moirai.storyengine.core.application.usecase.adventure.request;

import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import org.assertj.core.util.Maps;
import org.junit.jupiter.api.Test;

import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.domain.world.WorldFixture;

public class UpdateAdventureTest {

    @Test
    public void updateAdventure_whenValidDate_thenInstanceIsCreated() {

        // Given
        Adventure adventure = AdventureFixture.privateMultiplayerAdventure().build();
        UpdateAdventure.Builder builder = UpdateAdventure.builder()
                .id(adventure.getId())
                .name(adventure.getName())
                .description(adventure.getDescription())
                .worldId(WorldFixture.PUBLIC_ID)
                .personaId(PersonaFixture.PUBLIC_ID)
                .channelId(adventure.getChannelId())
                .visibility(adventure.getVisibility().name())
                .aiModel(adventure.getModelConfiguration().getAiModel().toString())
                .moderation(adventure.getModeration().name())
                .maxTokenLimit(adventure.getModelConfiguration().getMaxTokenLimit())
                .temperature(adventure.getModelConfiguration().getTemperature())
                .frequencyPenalty(adventure.getModelConfiguration().getFrequencyPenalty())
                .presencePenalty(adventure.getModelConfiguration().getPresencePenalty())
                .stopSequencesToAdd(adventure.getModelConfiguration().getStopSequences())
                .stopSequencesToRemove(adventure.getModelConfiguration().getStopSequences())
                .logitBiasToAdd(Maps.newHashMap("TKNID", 99D))
                .logitBiasToRemove(Collections.singleton("TKN"))
                .usersAllowedToWriteToAdd(Collections.singleton("USRID"))
                .usersAllowedToWriteToRemove(Collections.singleton("USRID"))
                .usersAllowedToReadToAdd(Collections.singleton("USRID"))
                .usersAllowedToReadToRemove(Collections.singleton("USRID"))
                .gameMode(adventure.getGameMode().name())
                .requesterId(adventure.getOwnerId())
                .nudge(adventure.getContextAttributes().getNudge())
                .remember(adventure.getContextAttributes().getRemember())
                .authorsNote(adventure.getContextAttributes().getAuthorsNote())
                .bump(adventure.getContextAttributes().getBump())
                .bumpFrequency(adventure.getContextAttributes().getBumpFrequency())
                .isMultiplayer(adventure.isMultiplayer())
                .adventureStart(adventure.getAdventureStart())
                .maxTokenLimit(adventure.getModelConfiguration().getMaxTokenLimit());

        // When
        UpdateAdventure updateAdventure = builder.build();

        // Then
        assertThat(updateAdventure.getId()).isEqualTo(adventure.getId());
        assertThat(updateAdventure.getAdventureStart()).isEqualTo(adventure.getAdventureStart());
        assertThat(updateAdventure.getDescription()).isEqualTo(adventure.getDescription());
        assertThat(updateAdventure.getChannelId()).isEqualTo(adventure.getChannelId());
        assertThat(updateAdventure.getGameMode()).isEqualToIgnoringCase(adventure.getGameMode().name());
        assertThat(updateAdventure.getName()).isEqualTo(adventure.getName());
        assertThat(updateAdventure.getPersonaId()).isEqualTo(PersonaFixture.PUBLIC_ID);
        assertThat(updateAdventure.getWorldId()).isEqualTo(WorldFixture.PUBLIC_ID);
        assertThat(updateAdventure.getVisibility()).isEqualToIgnoringCase(adventure.getVisibility().name());
        assertThat(updateAdventure.getPresencePenalty())
                .isEqualTo(adventure.getModelConfiguration().getPresencePenalty());
        assertThat(updateAdventure.getFrequencyPenalty())
                .isEqualTo(adventure.getModelConfiguration().getFrequencyPenalty());
        assertThat(updateAdventure.getTemperature()).isEqualTo(adventure.getModelConfiguration().getTemperature());
        assertThat(updateAdventure.getMaxTokenLimit()).isEqualTo(adventure.getModelConfiguration().getMaxTokenLimit());
        assertThat(updateAdventure.getRemember()).isEqualTo(adventure.getContextAttributes().getRemember());
        assertThat(updateAdventure.getAuthorsNote()).isEqualTo(adventure.getContextAttributes().getAuthorsNote());
        assertThat(updateAdventure.getNudge()).isEqualTo(adventure.getContextAttributes().getNudge());
        assertThat(updateAdventure.getBump()).isEqualTo(adventure.getContextAttributes().getBump());
        assertThat(updateAdventure.getBumpFrequency()).isEqualTo(adventure.getContextAttributes().getBumpFrequency());

        assertThat(updateAdventure.getAiModel())
                .isEqualToIgnoringCase(adventure.getModelConfiguration().getAiModel().toString());
    }

    @Test
    public void updateAdventure_whenStopSequencesToAddIsNull_thenListIsEmpty() {

        // Given
        UpdateAdventure.Builder builder = UpdateAdventureFixture.sample()
                .stopSequencesToAdd(null);

        // When
        UpdateAdventure updateAdventure = builder.build();

        // Then
        assertThat(updateAdventure.getStopSequencesToAdd()).isNotNull().isEmpty();
    }

    @Test
    public void updateAdventure_whenStopSequencesToRemoveIsNull_thenListIsEmpty() {

        // Given
        UpdateAdventure.Builder builder = UpdateAdventureFixture.sample()
                .stopSequencesToRemove(null);

        // When
        UpdateAdventure updateAdventure = builder.build();

        // Then
        assertThat(updateAdventure.getStopSequencesToRemove()).isNotNull().isEmpty();
    }

    @Test
    public void updateAdventure_whenLogitBiasToAddIsNull_thenListIsEmpty() {

        // Given
        UpdateAdventure.Builder builder = UpdateAdventureFixture.sample()
                .logitBiasToAdd(null);

        // When
        UpdateAdventure updateAdventure = builder.build();

        // Then
        assertThat(updateAdventure.getLogitBiasToAdd()).isNotNull().isEmpty();
    }

    @Test
    public void updateAdventure_whenLogitBiasToRemoveIsNull_thenListIsEmpty() {

        // Given
        UpdateAdventure.Builder builder = UpdateAdventureFixture.sample()
                .logitBiasToRemove(null);

        // When
        UpdateAdventure updateAdventure = builder.build();

        // Then
        assertThat(updateAdventure.getLogitBiasToRemove()).isNotNull().isEmpty();
    }

    @Test
    public void updateAdventure_whenUsersAllowedToWriteToAddIsNull_thenListIsEmpty() {

        // Given
        UpdateAdventure.Builder builder = UpdateAdventureFixture.sample()
                .usersAllowedToWriteToAdd(null);

        // When
        UpdateAdventure updateAdventure = builder.build();

        // Then
        assertThat(updateAdventure.getUsersAllowedToWriteToAdd()).isNotNull().isEmpty();
    }

    @Test
    public void updateAdventure_whenUsersAllowedToWriteToRemoveIsNull_thenListIsEmpty() {

        // Given
        UpdateAdventure.Builder builder = UpdateAdventureFixture.sample()
                .usersAllowedToWriteToRemove(null);

        // When
        UpdateAdventure updateAdventure = builder.build();

        // Then
        assertThat(updateAdventure.getUsersAllowedToWriteToRemove()).isNotNull().isEmpty();
    }

    @Test
    public void updateAdventure_whenUsersAllowedToReadToAddIsNull_thenListIsEmpty() {

        // Given
        UpdateAdventure.Builder builder = UpdateAdventureFixture.sample()
                .usersAllowedToReadToAdd(null);

        // When
        UpdateAdventure updateAdventure = builder.build();

        // Then
        assertThat(updateAdventure.getUsersAllowedToReadToAdd()).isNotNull().isEmpty();
    }

    @Test
    public void updateAdventure_whenUsersAllowedToReadToRemoveIsNull_thenListIsEmpty() {

        // Given
        UpdateAdventure.Builder builder = UpdateAdventureFixture.sample()
                .usersAllowedToReadToRemove(null);

        // When
        UpdateAdventure updateAdventure = builder.build();

        // Then
        assertThat(updateAdventure.getUsersAllowedToReadToRemove()).isNotNull().isEmpty();
    }
}
