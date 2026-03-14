package me.moirai.storyengine.core.application.usecase.adventure;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventureRememberByChannelId;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.domain.PermissionsFixture;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;

@ExtendWith(MockitoExtension.class)
public class UpdateAdventureRememberByChannelIdHandlerTest {

    @Mock
    private AdventureRepository repository;

    @InjectMocks
    private UpdateAdventureRememberByChannelIdHandler handler;

    @Test
    public void updateRemember_whenAdventureNotFound_thenThrowException() {

        // Given
        String requesterId = "123123";
        UpdateAdventureRememberByChannelId command = UpdateAdventureRememberByChannelId.builder()
                .remember("Remember")
                .requesterId(requesterId)
                .channelId("1234123")
                .build();

        when(repository.findByChannelId(anyString())).thenReturn(Optional.empty());

        // Then
        assertThrows(AssetNotFoundException.class, () -> handler.handle(command));
    }

    @Test
    public void updateRemember_whenNoAdventurePermission_thenThrowException() {

        // Given
        String adventureId = "123123";
        String requesterId = "123123";
        UpdateAdventureRememberByChannelId command = UpdateAdventureRememberByChannelId.builder()
                .remember("Remember")
                .requesterId(requesterId)
                .channelId("1234123")
                .build();

        Adventure adventure = AdventureFixture.privateMultiplayerAdventure()
                .id(adventureId)
                .build();

        when(repository.findByChannelId(anyString())).thenReturn(Optional.of(adventure));

        // Then
        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(AssetAccessDeniedException.class)
                .hasMessage("User does not have permission to update adventure");
    }

    @Test
    public void updateRemember_whenCalled_thenUpdateAdventureRemember() {

        // Given
        String requesterId = "4245345";
        UpdateAdventureRememberByChannelId command = UpdateAdventureRememberByChannelId.builder()
                .remember("Remember")
                .channelId("1234123")
                .requesterId(requesterId)
                .channelId("1234123")
                .build();

        Adventure adventure = AdventureFixture.privateMultiplayerAdventure()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        when(repository.findByChannelId(anyString())).thenReturn(Optional.of(adventure));

        // When
        handler.execute(command);

        // Then
        verify(repository, times(1))
                .updateRememberByChannelId(anyString(), anyString());
    }
}
