package me.moirai.storyengine.core.application.usecase.adventure.request;

import me.moirai.storyengine.core.port.inbound.adventure.CreateAdventure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import org.assertj.core.util.Maps;
import org.junit.jupiter.api.Test;

import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;

public class CreateAdventureTest {

    @Test
    public void updateAdventure_whenValidDate_thenInstanceIsCreated() {

        // Given
        Adventure adventure = AdventureFixture.privateMultiplayerAdventure().build();
        CreateAdventure.Builder builder = CreateAdventure.builder()
                .name(adventure.getName())
                .description(adventure.getDescription())
                .worldId(adventure.getWorldId())
                .personaId(adventure.getPersonaId())
                .channelId(adventure.getChannelId())
                .visibility(adventure.getVisibility().name())
                .aiModel(adventure.getModelConfiguration().getAiModel().toString())
                .moderation(adventure.getModeration().name())
                .maxTokenLimit(adventure.getModelConfiguration().getMaxTokenLimit())
                .temperature(adventure.getModelConfiguration().getTemperature())
                .frequencyPenalty(adventure.getModelConfiguration().getFrequencyPenalty())
                .presencePenalty(adventure.getModelConfiguration().getPresencePenalty())
                .stopSequences(adventure.getModelConfiguration().getStopSequences())
                .logitBias(Maps.newHashMap("TKNID", 99D))
                .usersAllowedToWrite(Collections.singleton("USRID"))
                .usersAllowedToRead(Collections.singleton("USRID"))
                .gameMode(adventure.getGameMode().name())
                .requesterId(adventure.getOwnerId())
                .nudge(adventure.getContextAttributes().getNudge())
                .remember(adventure.getContextAttributes().getRemember())
                .authorsNote(adventure.getContextAttributes().getAuthorsNote())
                .bump(adventure.getContextAttributes().getBump())
                .bumpFrequency(adventure.getContextAttributes().getBumpFrequency())
                .isMultiplayer(adventure.isMultiplayer())
                .maxTokenLimit(adventure.getModelConfiguration().getMaxTokenLimit());

        // When
        CreateAdventure updateAdventure = builder.build();

        // Then
        assertThat(updateAdventure.getDescription()).isEqualTo(adventure.getDescription());
        assertThat(updateAdventure.getChannelId()).isEqualTo(adventure.getChannelId());
        assertThat(updateAdventure.getGameMode()).isEqualToIgnoringCase(adventure.getGameMode().name());
        assertThat(updateAdventure.getName()).isEqualTo(adventure.getName());
        assertThat(updateAdventure.getPersonaId()).isEqualTo(adventure.getPersonaId());
        assertThat(updateAdventure.getWorldId()).isEqualTo(adventure.getWorldId());
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
    public void updateAdventure_whenStopSequencesIsNull_thenListIsEmpty() {

        // Given
        CreateAdventure.Builder builder = CreateAdventureFixture.sample()
                .stopSequences(null);

        // When
        CreateAdventure updateAdventure = builder.build();

        // Then
        assertThat(updateAdventure.getStopSequences()).isNotNull().isEmpty();
    }

    @Test
    public void updateAdventure_whenLogitBiasIsNull_thenListIsEmpty() {

        // Given
        CreateAdventure.Builder builder = CreateAdventureFixture.sample()
                .logitBias(null);

        // When
        CreateAdventure updateAdventure = builder.build();

        // Then
        assertThat(updateAdventure.getLogitBias()).isNotNull().isEmpty();
    }

    @Test
    public void updateAdventure_whenUsersAllowedToWriteIsNull_thenListIsEmpty() {

        // Given
        CreateAdventure.Builder builder = CreateAdventureFixture.sample()
                .usersAllowedToWrite(null);

        // When
        CreateAdventure updateAdventure = builder.build();

        // Then
        assertThat(updateAdventure.getUsersAllowedToWrite()).isNotNull().isEmpty();
    }

    @Test
    public void updateAdventure_whenUsersAllowedToReadIsNull_thenListIsEmpty() {

        // Given
        CreateAdventure.Builder builder = CreateAdventureFixture.sample()
                .usersAllowedToRead(null);

        // When
        CreateAdventure updateAdventure = builder.build();

        // Then
        assertThat(updateAdventure.getUsersAllowedToRead()).isNotNull().isEmpty();
    }
}
