package me.moirai.storyengine.core.application.usecase.adventure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import me.moirai.storyengine.core.application.usecase.adventure.request.GetAdventureById;
import me.moirai.storyengine.core.application.usecase.adventure.result.GetAdventureResult;
import me.moirai.storyengine.core.domain.PermissionsFixture;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.adventure.AdventureRepository;

@ExtendWith(MockitoExtension.class)
public class GetAdventureByIdHandlerTest {

    @Mock
    private AdventureRepository queryRepository;

    @InjectMocks
    private GetAdventureByIdHandler handler;

    @Test
    public void errorWhenIdIsNull() {

        // Given
        String id = null;
        String requesterId = "123123";
        GetAdventureById query = GetAdventureById.build(id, requesterId);

        // Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(query));
    }

    @Test
    public void errorWhenQueryIsNull() {

        // Given
        GetAdventureById query = null;

        // Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(query));
    }

    @Test
    public void getAdventure_whenNoAdventurePermission_thenThrowException() {

        // Given
        String adventureId = "123123";
        String requesterId = "123123";
        GetAdventureById command = GetAdventureById.build(adventureId, requesterId);
        Adventure adventure = AdventureFixture.privateMultiplayerAdventure()
                .id(adventureId)
                .build();

        when(queryRepository.findById(anyString())).thenReturn(Optional.of(adventure));

        // Then
        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(AssetAccessDeniedException.class)
                .hasMessage("User does not have permission to view adventure");
    }

    @Test
    public void getAdventureById() {

        // Given
        String id = "HAUDHUAHD";
        String requesterId = "RQSTRID";
        Adventure adventure = AdventureFixture.privateMultiplayerAdventure()
                .id(id)
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        GetAdventureById query = GetAdventureById.build(id, requesterId);

        when(queryRepository.findById(anyString())).thenReturn(Optional.of(adventure));

        // When
        GetAdventureResult result = handler.handle(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getId()).isEqualTo(adventure.getId());
        assertThat(result.getAdventureStart()).isEqualTo(adventure.getAdventureStart());
        assertThat(result.getDescription()).isEqualTo(adventure.getDescription());
        assertThat(result.getChannelId()).isEqualTo(adventure.getChannelId());
        assertThat(result.getGameMode()).isEqualTo(adventure.getGameMode().name());
        assertThat(result.getName()).isEqualTo(adventure.getName());
        assertThat(result.getOwnerId()).isEqualTo(adventure.getOwnerId());
        assertThat(result.getPersonaId()).isEqualTo(adventure.getPersonaId());
        assertThat(result.getVisibility()).isEqualTo(adventure.getVisibility().name());
        assertThat(result.getModeration()).isEqualTo(adventure.getModeration().name());
        assertThat(result.getWorldId()).isEqualTo(adventure.getWorldId());
        assertThat(result.isMultiplayer()).isEqualTo(adventure.isMultiplayer());
        assertThat(result.getCreationDate()).isNotNull();
        assertThat(result.getLastUpdateDate()).isNotNull();

        assertThat(result.getAuthorsNote()).isEqualTo(adventure.getContextAttributes().getAuthorsNote());
        assertThat(result.getNudge()).isEqualTo(adventure.getContextAttributes().getNudge());
        assertThat(result.getRemember()).isEqualTo(adventure.getContextAttributes().getRemember());
        assertThat(result.getBump()).isEqualTo(adventure.getContextAttributes().getBump());
        assertThat(result.getBumpFrequency()).isEqualTo(adventure.getContextAttributes().getBumpFrequency());

        assertThat(result.getAiModel()).isEqualToIgnoringCase(adventure.getModelConfiguration().getAiModel().toString());
        assertThat(result.getFrequencyPenalty()).isEqualTo(adventure.getModelConfiguration().getFrequencyPenalty());
        assertThat(result.getLogitBias()).isEqualTo(adventure.getModelConfiguration().getLogitBias());
        assertThat(result.getMaxTokenLimit()).isEqualTo(adventure.getModelConfiguration().getMaxTokenLimit());
        assertThat(result.getPresencePenalty()).isEqualTo(adventure.getModelConfiguration().getPresencePenalty());
        assertThat(result.getStopSequences()).isEqualTo(adventure.getModelConfiguration().getStopSequences());
        assertThat(result.getTemperature()).isEqualTo(adventure.getModelConfiguration().getTemperature());

        assertThat(result.getUsersAllowedToRead()).hasSameElementsAs(adventure.getUsersAllowedToRead());
        assertThat(result.getUsersAllowedToWrite()).hasSameElementsAs(adventure.getUsersAllowedToWrite());
    }

    @Test
    public void findAdventure_whenAdventureNotFound_thenThrowException() {

        // Given
        String id = "ADVID";
        String requesterId = "123123";
        GetAdventureById query = GetAdventureById.build(id, requesterId);

        when(queryRepository.findById(anyString())).thenReturn(Optional.empty());

        // Then
        assertThrows(AssetNotFoundException.class, () -> handler.handle(query));
    }

    @Test
    public void findAdventure_whenValidId_thenAdventureIsReturned() {

        // Given
        String id = "ADVID";
        String requesterId = "RQSTRID";
        GetAdventureById query = GetAdventureById.build(id, requesterId);

        Adventure adventure = AdventureFixture.privateMultiplayerAdventure()
                .id(id)
                .name("New name")
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        when(queryRepository.findById(anyString())).thenReturn(Optional.of(adventure));

        // When
        GetAdventureResult result = handler.handle(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(adventure.getName());
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getId()).isEqualTo(adventure.getId());
        assertThat(result.getAdventureStart()).isEqualTo(adventure.getAdventureStart());
        assertThat(result.getDescription()).isEqualTo(adventure.getDescription());
        assertThat(result.getChannelId()).isEqualTo(adventure.getChannelId());
        assertThat(result.getGameMode()).isEqualTo(adventure.getGameMode().name());
        assertThat(result.getName()).isEqualTo(adventure.getName());
        assertThat(result.getOwnerId()).isEqualTo(adventure.getOwnerId());
        assertThat(result.getPersonaId()).isEqualTo(adventure.getPersonaId());
        assertThat(result.getVisibility()).isEqualTo(adventure.getVisibility().name());
        assertThat(result.getModeration()).isEqualTo(adventure.getModeration().name());
        assertThat(result.getWorldId()).isEqualTo(adventure.getWorldId());
        assertThat(result.isMultiplayer()).isEqualTo(adventure.isMultiplayer());
        assertThat(result.getCreationDate()).isNotNull();
        assertThat(result.getLastUpdateDate()).isNotNull();

        assertThat(result.getAuthorsNote()).isEqualTo(adventure.getContextAttributes().getAuthorsNote());
        assertThat(result.getNudge()).isEqualTo(adventure.getContextAttributes().getNudge());
        assertThat(result.getRemember()).isEqualTo(adventure.getContextAttributes().getRemember());
        assertThat(result.getBump()).isEqualTo(adventure.getContextAttributes().getBump());
        assertThat(result.getBumpFrequency()).isEqualTo(adventure.getContextAttributes().getBumpFrequency());

        assertThat(result.getAiModel()).isEqualToIgnoringCase(adventure.getModelConfiguration().getAiModel().toString());
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