package me.moirai.storyengine.core.application.query.adventure;

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

import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.core.domain.PermissionsFixture;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureDetails;
import me.moirai.storyengine.core.port.inbound.adventure.GetAdventureByChannelId;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
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
    public void getAdventure_whenAdventureIsFound_thenReturnResult() {

        // Given
        String adventureId = "123123";
        String requesterId = "123123";
        GetAdventureByChannelId command = GetAdventureByChannelId.build(adventureId, requesterId);
        Adventure adventure = AdventureFixture.privateMultiplayerAdventure()
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
        assertThat(result.id()).isEqualTo(adventure.getPublicId());
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

        assertThat(result.contextAttributes().authorsNote()).isEqualTo(adventure.getContextAttributes().authorsNote());
        assertThat(result.contextAttributes().nudge()).isEqualTo(adventure.getContextAttributes().nudge());
        assertThat(result.contextAttributes().remember()).isEqualTo(adventure.getContextAttributes().remember());
        assertThat(result.contextAttributes().bump()).isEqualTo(adventure.getContextAttributes().bump());
        assertThat(result.contextAttributes().bumpFrequency()).isEqualTo(adventure.getContextAttributes().bumpFrequency());

        assertThat(result.modelConfiguration().aiModel())
                .isEqualTo(adventure.getModelConfiguration().getAiModel());
        assertThat(result.modelConfiguration().frequencyPenalty()).isEqualTo(adventure.getModelConfiguration().getFrequencyPenalty());
        assertThat(result.modelConfiguration().logitBias()).isEqualTo(adventure.getModelConfiguration().getLogitBias());
        assertThat(result.modelConfiguration().maxTokenLimit()).isEqualTo(adventure.getModelConfiguration().getMaxTokenLimit());
        assertThat(result.modelConfiguration().presencePenalty()).isEqualTo(adventure.getModelConfiguration().getPresencePenalty());
        assertThat(result.modelConfiguration().stopSequences()).isEqualTo(adventure.getModelConfiguration().getStopSequences());
        assertThat(result.modelConfiguration().temperature()).isEqualTo(adventure.getModelConfiguration().getTemperature());
    }
}
