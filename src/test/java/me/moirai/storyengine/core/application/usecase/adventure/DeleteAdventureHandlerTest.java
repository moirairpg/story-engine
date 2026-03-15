package me.moirai.storyengine.core.application.usecase.adventure;

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
import me.moirai.storyengine.core.port.inbound.adventure.DeleteAdventure;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.domain.PermissionsFixture;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;

@ExtendWith(MockitoExtension.class)
public class DeleteAdventureHandlerTest {

    @Mock
    private AdventureRepository repository;

    @InjectMocks
    private DeleteAdventureHandler handler;

    @Test
    public void errorWhenIdIsNull() {

        // Given
        String id = null;
        String requesterId = "RQSTRID";
        DeleteAdventure command = DeleteAdventure.build(id, requesterId);

        // Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void deleteAdventure_whenNoAdventurePermission_thenThrowException() {

        // Given
        String id = AdventureFixture.PUBLIC_ID;
        String requesterId = "RQSTRID";
        DeleteAdventure command = DeleteAdventure.build(id, requesterId);

        Adventure adventure = AdventureFixture.privateMultiplayerAdventure()
                .name("New name")
                .build();

        when(repository.findByPublicId(anyString())).thenReturn(Optional.of(adventure));

        // Then
        assertThrows(AssetAccessDeniedException.class, () -> handler.handle(command));
    }

    @Test
    public void deleteAdventure_whenAdventureNotFound_thenThrowException() {

        // Given
        String id = AdventureFixture.PUBLIC_ID;
        String requesterId = "RQSTRID";
        DeleteAdventure command = DeleteAdventure.build(id, requesterId);

        when(repository.findByPublicId(anyString())).thenReturn(Optional.empty());

        // Then
        assertThrows(AssetNotFoundException.class, () -> handler.handle(command));
    }

    @Test
    public void deleteAdventure_whenProperPermission_thenAdventureIsDeleted() {

        // Given
        String id = AdventureFixture.PUBLIC_ID;
        String requesterId = "RQSTRID";
        DeleteAdventure command = DeleteAdventure.build(id, requesterId);

        Adventure adventure = AdventureFixture.privateMultiplayerAdventure()
                .name("New name")
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        when(repository.findByPublicId(anyString())).thenReturn(Optional.of(adventure));

        // When
        handler.handle(command);

        // Then
        verify(repository, times(1)).findByPublicId(anyString());
        verify(repository, times(1)).deleteByPublicId(anyString());
    }
}
