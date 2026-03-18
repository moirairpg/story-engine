package me.moirai.storyengine.core.application.query.adventure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.core.domain.PermissionsFixture;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureDetails;
import me.moirai.storyengine.core.port.inbound.adventure.GetAdventureById;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

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
        GetAdventureById query = new GetAdventureById(null, "123123");

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

        GetAdventureById query = new GetAdventureById(AdventureFixture.PUBLIC_ID, requesterId);

        when(queryRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(personaRepository.findById(anyLong())).thenReturn(Optional.of(PersonaFixture.publicPersonaWithId()));
        when(worldRepository.findById(anyLong())).thenReturn(Optional.of(WorldFixture.publicWorldWithId()));

        // When
        AdventureDetails result = handler.handle(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(AdventureFixture.PUBLIC_ID);
        assertThat(result.adventureStart()).isEqualTo(adventure.getAdventureStart());
        assertThat(result.description()).isEqualTo(adventure.getDescription());
        assertThat(result.channelId()).isEqualTo(adventure.getChannelId());
        assertThat(result.gameMode()).isEqualTo(adventure.getGameMode().name());
        assertThat(result.name()).isEqualTo(adventure.getName());
        assertThat(result.ownerId()).isEqualTo(adventure.getOwnerId());
        assertThat(result.personaId()).isEqualTo(PersonaFixture.PUBLIC_ID);
        assertThat(result.visibility()).isEqualTo(adventure.getVisibility().name());
        assertThat(result.moderation()).isEqualTo(adventure.getModeration().name());
        assertThat(result.worldId()).isEqualTo(WorldFixture.PUBLIC_ID);
        assertThat(result.isMultiplayer()).isEqualTo(adventure.isMultiplayer());
        assertThat(result.creationDate()).isNotNull();
        assertThat(result.lastUpdateDate()).isNotNull();

        assertThat(result.authorsNote()).isEqualTo(adventure.getContextAttributes().authorsNote());
        assertThat(result.nudge()).isEqualTo(adventure.getContextAttributes().nudge());
        assertThat(result.remember()).isEqualTo(adventure.getContextAttributes().remember());
        assertThat(result.bump()).isEqualTo(adventure.getContextAttributes().bump());
        assertThat(result.bumpFrequency()).isEqualTo(adventure.getContextAttributes().bumpFrequency());

        assertThat(result.aiModel()).isEqualToIgnoringCase(adventure.getModelConfiguration().aiModel().toString());
        assertThat(result.frequencyPenalty()).isEqualTo(adventure.getModelConfiguration().frequencyPenalty());
        assertThat(result.logitBias()).isEqualTo(adventure.getModelConfiguration().logitBias());
        assertThat(result.maxTokenLimit()).isEqualTo(adventure.getModelConfiguration().maxTokenLimit());
        assertThat(result.presencePenalty()).isEqualTo(adventure.getModelConfiguration().presencePenalty());
        assertThat(result.stopSequences()).isEqualTo(adventure.getModelConfiguration().stopSequences());
        assertThat(result.temperature()).isEqualTo(adventure.getModelConfiguration().temperature());

        assertThat(result.usersAllowedToRead()).hasSameElementsAs(adventure.getUsersAllowedToRead());
        assertThat(result.usersAllowedToWrite()).hasSameElementsAs(adventure.getUsersAllowedToWrite());
    }

    @Test
    public void findAdventure_whenAdventureNotFound_thenThrowException() {

        // Given
        GetAdventureById query = new GetAdventureById(AdventureFixture.PUBLIC_ID, "123123");

        when(queryRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

        // Then
        assertThrows(AssetNotFoundException.class, () -> handler.handle(query));
    }

    @Test
    public void findAdventure_whenValidId_thenAdventureIsReturned() {

        // Given
        String requesterId = "RQSTRID";
        GetAdventureById query = new GetAdventureById(AdventureFixture.PUBLIC_ID, requesterId);

        Adventure adventure = AdventureFixture.privateMultiplayerAdventure()
                .name("New name")
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();
        ReflectionTestUtils.setField(adventure, "id", AdventureFixture.NUMERIC_ID);
        ReflectionTestUtils.setField(adventure, "publicId", AdventureFixture.PUBLIC_ID);

        when(queryRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(personaRepository.findById(anyLong())).thenReturn(Optional.of(PersonaFixture.publicPersonaWithId()));
        when(worldRepository.findById(anyLong())).thenReturn(Optional.of(WorldFixture.publicWorldWithId()));

        // When
        AdventureDetails result = handler.handle(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo(adventure.getName());
        assertThat(result.id()).isEqualTo(AdventureFixture.PUBLIC_ID);
        assertThat(result.adventureStart()).isEqualTo(adventure.getAdventureStart());
        assertThat(result.description()).isEqualTo(adventure.getDescription());
        assertThat(result.channelId()).isEqualTo(adventure.getChannelId());
        assertThat(result.gameMode()).isEqualTo(adventure.getGameMode().name());
        assertThat(result.ownerId()).isEqualTo(adventure.getOwnerId());
        assertThat(result.personaId()).isEqualTo(PersonaFixture.PUBLIC_ID);
        assertThat(result.visibility()).isEqualTo(adventure.getVisibility().name());
        assertThat(result.moderation()).isEqualTo(adventure.getModeration().name());
        assertThat(result.worldId()).isEqualTo(WorldFixture.PUBLIC_ID);
        assertThat(result.isMultiplayer()).isEqualTo(adventure.isMultiplayer());
        assertThat(result.creationDate()).isNotNull();
        assertThat(result.lastUpdateDate()).isNotNull();

        assertThat(result.authorsNote()).isEqualTo(adventure.getContextAttributes().authorsNote());
        assertThat(result.nudge()).isEqualTo(adventure.getContextAttributes().nudge());
        assertThat(result.remember()).isEqualTo(adventure.getContextAttributes().remember());
        assertThat(result.bump()).isEqualTo(adventure.getContextAttributes().bump());
        assertThat(result.bumpFrequency()).isEqualTo(adventure.getContextAttributes().bumpFrequency());

        assertThat(result.aiModel()).isEqualToIgnoringCase(adventure.getModelConfiguration().aiModel().toString());
        assertThat(result.frequencyPenalty()).isEqualTo(adventure.getModelConfiguration().frequencyPenalty());
        assertThat(result.logitBias()).isEqualTo(adventure.getModelConfiguration().logitBias());
        assertThat(result.maxTokenLimit()).isEqualTo(adventure.getModelConfiguration().maxTokenLimit());
        assertThat(result.presencePenalty()).isEqualTo(adventure.getModelConfiguration().presencePenalty());
        assertThat(result.stopSequences()).isEqualTo(adventure.getModelConfiguration().stopSequences());
        assertThat(result.temperature()).isEqualTo(adventure.getModelConfiguration().temperature());

        assertThat(result.usersAllowedToRead()).hasSameElementsAs(adventure.getUsersAllowedToRead());
        assertThat(result.usersAllowedToWrite()).hasSameElementsAs(adventure.getUsersAllowedToWrite());
    }
}
