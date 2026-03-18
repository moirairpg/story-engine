package me.moirai.storyengine.core.application.command.adventure;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.port.inbound.adventure.DeleteAdventure;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;

@ExtendWith(MockitoExtension.class)
public class DeleteAdventureHandlerTest {

    @Mock
    private AdventureRepository repository;

    @InjectMocks
    private DeleteAdventureHandler handler;

    @Test
    public void errorWhenIdIsNull() {

        // Given
        String requesterId = "RQSTRID";
        DeleteAdventure command = new DeleteAdventure(null, requesterId);

        // Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void deleteAdventure_whenNoAdventurePermission_thenThrowException() {

        // Given
        String requesterId = "RQSTRID";
        DeleteAdventure command = new DeleteAdventure(AdventureFixture.PUBLIC_ID, requesterId);

        Adventure adventure = AdventureFixture.privateMultiplayerAdventure()
                .name("New name")
                .build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(adventure));

        // Then
        assertThrows(AssetAccessDeniedException.class, () -> handler.handle(command));
    }

    @Test
    public void deleteAdventure_whenAdventureNotFound_thenThrowException() {

        // Given
        String requesterId = "RQSTRID";
        DeleteAdventure command = new DeleteAdventure(AdventureFixture.PUBLIC_ID, requesterId);

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

        // Then
        assertThrows(AssetNotFoundException.class, () -> handler.handle(command));
    }
}
