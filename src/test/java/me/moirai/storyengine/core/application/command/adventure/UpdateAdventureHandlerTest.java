package me.moirai.storyengine.core.application.command.adventure;

import static me.moirai.storyengine.common.enums.Visibility.PUBLIC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.enums.Visibility;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.domain.world.WorldFixture;
import me.moirai.storyengine.core.port.inbound.UpdateAdventureFixture;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventure;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

@ExtendWith(MockitoExtension.class)
public class UpdateAdventureHandlerTest {

    @Mock
    private AdventureRepository repository;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private WorldRepository worldRepository;

    @InjectMocks
    private UpdateAdventureHandler handler;

    @Test
    public void errorWhenIdIsNull() {

        // given
        var command = new UpdateAdventure(
                null,
                null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, false);

        // then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void updateAdventure() {

        // given
        var requesterId = "DASDASD";
        var command = UpdateAdventureFixture.sampleWithRequesterId(requesterId);

        var expectedUpdatedAdventure = AdventureFixture.privateMultiplayerAdventure().build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(expectedUpdatedAdventure));
        when(repository.save(any())).thenReturn(expectedUpdatedAdventure);
        when(personaRepository.findByPublicId(any(UUID.class)))
                .thenReturn(Optional.of(PersonaFixture.publicPersonaWithId()));

        when(worldRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(WorldFixture.publicWorldWithId()));

        // when
        var result = handler.handle(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.lastUpdateDate()).isEqualTo(expectedUpdatedAdventure.getLastUpdateDate());
    }

    @Test
    public void updateAdventure_whenAdventureToUpdateNotFound_thenThrowException() {

        // given
        var requesterUserId = "LALALA";
        var updateAdventure = UpdateAdventureFixture.sampleWithRequesterId(requesterUserId);

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

        // then
        assertThrows(AssetNotFoundException.class, () -> handler.execute(updateAdventure));
    }

    @Test
    public void updateAdventure_whenPrivateToBeMadePublic_thenAdventureIsMadePublic() {

        // given
        var requesterId = "RQSTRID";
        var command = UpdateAdventureFixture.sampleWithVisibility(requesterId, PUBLIC);

        var unchangedAdventure = AdventureFixture.privateMultiplayerAdventure()
                .visibility(Visibility.PRIVATE)
                .build();

        var expectedUpdatedAdventure = AdventureFixture.privateMultiplayerAdventure()
                .visibility(Visibility.PUBLIC)
                .build();

        var adventureCaptor = ArgumentCaptor.forClass(Adventure.class);

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(unchangedAdventure));
        when(repository.save(adventureCaptor.capture())).thenReturn(expectedUpdatedAdventure);
        when(personaRepository.findByPublicId(any(UUID.class)))
                .thenReturn(Optional.of(PersonaFixture.publicPersonaWithId()));

        when(worldRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(WorldFixture.publicWorldWithId()));

        // when
        handler.execute(command);

        // then
        var capturedAdventure = adventureCaptor.getValue();
        assertThat(capturedAdventure.getVisibility()).isEqualTo(unchangedAdventure.getVisibility());
    }

    @Test
    public void updateAdventure_whenAdventureIsSingleplayer_thenUpdateToMultiplayer() {

        // given
        var requesterId = "RQSTRID";
        var command = UpdateAdventureFixture.sampleWithMultiplayer(requesterId, true);

        var adventure = AdventureFixture.privateMultiplayerAdventure().build();

        var adventureCaptor = ArgumentCaptor.forClass(Adventure.class);

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(repository.save(adventureCaptor.capture())).thenReturn(adventure);
        when(personaRepository.findByPublicId(any(UUID.class)))
                .thenReturn(Optional.of(PersonaFixture.publicPersonaWithId()));

        when(worldRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(WorldFixture.publicWorldWithId()));

        // when
        handler.execute(command);

        // then
        var capturedAdventure = adventureCaptor.getValue();
        assertThat(capturedAdventure.isMultiplayer()).isTrue();
    }

    @Test
    public void updateAdventure_whenAdventureIsMultiplayer_thenUpdateToSingleplayer() {

        // given
        var requesterId = "RQSTRID";
        var command = UpdateAdventureFixture.sampleWithMultiplayer(requesterId, false);

        var adventure = AdventureFixture.privateMultiplayerAdventure().build();

        var adventureCaptor = ArgumentCaptor.forClass(Adventure.class);

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(repository.save(adventureCaptor.capture())).thenReturn(adventure);
        when(personaRepository.findByPublicId(any(UUID.class)))
                .thenReturn(Optional.of(PersonaFixture.publicPersonaWithId()));

        when(worldRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(WorldFixture.publicWorldWithId()));

        // when
        handler.execute(command);

        // then
        var capturedAdventure = adventureCaptor.getValue();
        assertThat(capturedAdventure.isMultiplayer()).isFalse();
    }
}
