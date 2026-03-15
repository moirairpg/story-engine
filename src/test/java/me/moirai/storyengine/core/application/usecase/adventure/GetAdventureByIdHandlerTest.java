package me.moirai.storyengine.core.application.usecase.adventure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.core.domain.PermissionsFixture;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureDetails;
import me.moirai.storyengine.core.port.inbound.adventure.GetAdventureById;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;
import me.moirai.storyengine.core.domain.world.WorldFixture;

@ExtendWith(MockitoExtension.class)
public class GetAdventureByIdHandlerTest {

    @Mock
    private AdventureRepository queryRepository;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private WorldRepository worldRepository;

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
        String adventureId = AdventureFixture.PUBLIC_ID;
        String requesterId = "123123";
        GetAdventureById command = GetAdventureById.build(adventureId, requesterId);
        Adventure adventure = AdventureFixture.privateMultiplayerAdventure().build();

        when(queryRepository.findByPublicId(anyString())).thenReturn(Optional.of(adventure));

        // Then
        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(AssetAccessDeniedException.class)
                .hasMessage("User does not have permission to view adventure");
    }

    @Test
    public void getAdventureById() {

        // Given
        String requesterId = "RQSTRID";
        Adventure adventure = AdventureFixture.privateMultiplayerAdventure()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", AdventureFixture.PUBLIC_ID);

        GetAdventureById query = GetAdventureById.build(AdventureFixture.PUBLIC_ID, requesterId);

        when(queryRepository.findByPublicId(anyString())).thenReturn(Optional.of(adventure));
        when(personaRepository.findById(anyLong())).thenReturn(Optional.of(PersonaFixture.publicPersonaWithId()));
        when(worldRepository.findById(anyLong())).thenReturn(Optional.of(WorldFixture.publicWorldWithId()));

        // When
        AdventureDetails result = handler.handle(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(AdventureFixture.PUBLIC_ID);
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
        String id = AdventureFixture.PUBLIC_ID;
        String requesterId = "123123";
        GetAdventureById query = GetAdventureById.build(id, requesterId);

        when(queryRepository.findByPublicId(anyString())).thenReturn(Optional.empty());

        // Then
        assertThrows(AssetNotFoundException.class, () -> handler.handle(query));
    }

    @Test
    public void findAdventure_whenValidId_thenAdventureIsReturned() {

        // Given
        String requesterId = "RQSTRID";
        GetAdventureById query = GetAdventureById.build(AdventureFixture.PUBLIC_ID, requesterId);

        Adventure adventure = AdventureFixture.privateMultiplayerAdventure()
                .name("New name")
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", AdventureFixture.PUBLIC_ID);

        when(queryRepository.findByPublicId(anyString())).thenReturn(Optional.of(adventure));
        when(personaRepository.findById(anyLong())).thenReturn(Optional.of(PersonaFixture.publicPersonaWithId()));
        when(worldRepository.findById(anyLong())).thenReturn(Optional.of(WorldFixture.publicWorldWithId()));

        // When
        AdventureDetails result = handler.handle(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(adventure.getName());
        assertThat(result.getId()).isEqualTo(AdventureFixture.PUBLIC_ID);
        assertThat(result.getAdventureStart()).isEqualTo(adventure.getAdventureStart());
        assertThat(result.getDescription()).isEqualTo(adventure.getDescription());
        assertThat(result.getChannelId()).isEqualTo(adventure.getChannelId());
        assertThat(result.getGameMode()).isEqualTo(adventure.getGameMode().name());
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
