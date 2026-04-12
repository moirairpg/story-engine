package me.moirai.storyengine.core.application.command.adventure;

import static me.moirai.storyengine.common.enums.Visibility.PUBLIC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.dto.PermissionDto;
import me.moirai.storyengine.common.enums.PermissionLevel;
import me.moirai.storyengine.common.enums.Visibility;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.port.inbound.UpdateAdventureFixture;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventure;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;
import me.moirai.storyengine.core.domain.userdetails.UserFixture;

@ExtendWith(MockitoExtension.class)
public class UpdateAdventureHandlerTest {

    @Mock
    private AdventureRepository repository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UpdateAdventureHandler handler;

    @Test
    public void errorWhenIdIsNull() {

        // given
        var command = new UpdateAdventure(
                null,
                null, null, null, null, null, null, null, false,
                null, null, null);

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
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(UserFixture.playerWithId()));

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
        assertThrows(NotFoundException.class, () -> handler.execute(updateAdventure));
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
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(UserFixture.playerWithId()));

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
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(UserFixture.playerWithId()));

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
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(UserFixture.playerWithId()));

        // when
        handler.execute(command);

        // then
        var capturedAdventure = adventureCaptor.getValue();
        assertThat(capturedAdventure.isMultiplayer()).isFalse();
    }

    @Test
    public void shouldOverwritePermissionsWhenUpdateAdventure() {

        // given
        var permissionDto = new PermissionDto(UserFixture.PUBLIC_ID, PermissionLevel.READ);
        var sample = UpdateAdventureFixture.sample();
        var command = new UpdateAdventure(
                sample.adventureId(),
                sample.name(),
                sample.description(),
                sample.adventureStart(),
                sample.narratorName(),
                sample.narratorPersonality(),
                sample.visibility(),
                sample.moderation(),
                sample.isMultiplayer(),
                Set.of(permissionDto),
                sample.modelConfiguration(),
                sample.contextAttributes());

        var adventure = AdventureFixture.privateMultiplayerAdventure().build();
        var user = UserFixture.playerWithId();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(userRepository.findByPublicId(UserFixture.PUBLIC_ID)).thenReturn(Optional.of(user));
        when(repository.save(any(Adventure.class))).thenReturn(adventure);
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(user));

        // when
        handler.execute(command);

        // then
        assertThat(adventure.getPermissions()).isNotNull();
    }

    @Test
    public void shouldUpdateNarratorWhenNarratorFieldsAreProvided() {

        // given
        var sample = UpdateAdventureFixture.sample();
        var command = new UpdateAdventure(
                sample.adventureId(),
                sample.name(),
                sample.description(),
                sample.adventureStart(),
                "Elan",
                "A wise elder narrator",
                sample.visibility(),
                sample.moderation(),
                sample.isMultiplayer(),
                Set.of(),
                sample.modelConfiguration(),
                sample.contextAttributes());

        var adventure = AdventureFixture.privateMultiplayerAdventure().build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));
        when(repository.save(any(Adventure.class))).thenReturn(adventure);
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(UserFixture.playerWithId()));

        // when
        var result = handler.handle(command);

        // then
        assertThat(result).isNotNull();
    }
}
