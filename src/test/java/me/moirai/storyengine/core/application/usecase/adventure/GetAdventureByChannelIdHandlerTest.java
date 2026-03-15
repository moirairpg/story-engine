package me.moirai.storyengine.core.application.usecase.adventure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.core.port.inbound.adventure.GetAdventureByChannelId;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureDetails;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.domain.PermissionsFixture;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

@ExtendWith(MockitoExtension.class)
public class GetAdventureByChannelIdHandlerTest {

    @Mock
    private AdventureRepository queryRepository;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private WorldRepository worldRepository;

    @InjectMocks
    private GetAdventureByChannelIdHandler handler;

    @Test
    public void getAdventure_whenAdventureNotFound_thenThrowException() {

        // Given
        String adventureId = "123123";
        String requesterId = "123123";
        GetAdventureByChannelId command = GetAdventureByChannelId.build(adventureId, requesterId);

        when(queryRepository.findByChannelId(anyString())).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(AssetNotFoundException.class)
                .hasMessage("No adventures exist for this channel");
    }

    @Test
    public void getAdventure_whenNoAdventurePermission_thenThrowException() {

        // Given
        String adventureId = "123123";
        String requesterId = "123123";
        GetAdventureByChannelId command = GetAdventureByChannelId.build(adventureId, requesterId);
        Adventure adventure = AdventureFixture.privateMultiplayerAdventure()
                .id(adventureId)
                .build();

        when(queryRepository.findByChannelId(anyString())).thenReturn(Optional.of(adventure));

        // Then
        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(AssetAccessDeniedException.class)
                .hasMessage("User does not have permission to view adventure");
    }

    @Test
    public void getAdventure_whenAdventureIsFound_thenReturnResult() {

        // Given
        String adventureId = "123123";
        String requesterId = "123123";
        GetAdventureByChannelId command = GetAdventureByChannelId.build(adventureId, requesterId);
        Adventure adventure = AdventureFixture.privateMultiplayerAdventure()
                .id(adventureId)
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        when(queryRepository.findByChannelId(anyString())).thenReturn(Optional.of(adventure));
        when(personaRepository.findById(anyLong())).thenReturn(Optional.of(PersonaFixture.publicPersonaWithId()));
        when(worldRepository.findById(anyLong())).thenReturn(Optional.of(WorldFixture.publicWorldWithId()));

        // When
        AdventureDetails result = handler.execute(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(adventureId);
        assertThat(result.getId()).isEqualTo(adventure.getId());
        assertThat(result.getAdventureStart()).isEqualTo(adventure.getAdventureStart());
        assertThat(result.getDescription()).isEqualTo(adventure.getDescription());
        assertThat(result.getChannelId()).isEqualTo(adventure.getChannelId());
        assertThat(result.getGameMode()).isEqualTo(adventure.getGameMode().name());
        assertThat(result.getName()).isEqualTo(adventure.getName());
        assertThat(result.getOwnerId()).isEqualTo(adventure.getOwnerId());
        assertThat(result.getPersonaId()).isEqualTo(PersonaFixture.PUBLIC_ID);
        assertThat(result.getVisibility()).isEqualTo(adventure.getVisibility().name());
        assertThat(result.getModeration()).isEqualTo(adventure.getModeration().name());
        assertThat(result.getWorldId()).isEqualTo(WorldFixture.PUBLIC_ID);
        assertThat(result.isMultiplayer()).isEqualTo(adventure.isMultiplayer());
        assertThat(result.getCreationDate()).isNotNull();
        assertThat(result.getLastUpdateDate()).isNotNull();

        assertThat(result.getAuthorsNote()).isEqualTo(adventure.getContextAttributes().getAuthorsNote());
        assertThat(result.getNudge()).isEqualTo(adventure.getContextAttributes().getNudge());
        assertThat(result.getRemember()).isEqualTo(adventure.getContextAttributes().getRemember());
        assertThat(result.getBump()).isEqualTo(adventure.getContextAttributes().getBump());
        assertThat(result.getBumpFrequency()).isEqualTo(adventure.getContextAttributes().getBumpFrequency());

        assertThat(result.getAiModel())
                .isEqualToIgnoringCase(adventure.getModelConfiguration().getAiModel().toString());
        assertThat(result.getFrequencyPenalty()).isEqualTo(adventure.getModelConfiguration().getFrequencyPenalty());
        assertThat(result.getLogitBias()).isEqualTo(adventure.getModelConfiguration().getLogitBias());
        assertThat(result.getMaxTokenLimit()).isEqualTo(adventure.getModelConfiguration().getMaxTokenLimit());
        assertThat(result.getPresencePenalty()).isEqualTo(adventure.getModelConfiguration().getPresencePenalty());
        assertThat(result.getStopSequences()).isEqualTo(adventure.getModelConfiguration().getStopSequences());
        assertThat(result.getTemperature()).isEqualTo(adventure.getModelConfiguration().getTemperature());

        assertThat(result.getUsersAllowedToRead()).hasSameElementsAs(adventure.getUsersAllowedToRead());
        assertThat(result.getUsersAllowedToWrite()).hasSameElementsAs(adventure.getUsersAllowedToWrite());
    }
}
