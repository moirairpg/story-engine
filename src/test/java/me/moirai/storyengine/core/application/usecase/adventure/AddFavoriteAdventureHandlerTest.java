package me.moirai.storyengine.core.application.usecase.adventure;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
import me.moirai.storyengine.core.port.inbound.adventure.AddFavoriteAdventure;
import me.moirai.storyengine.core.domain.adventure.Adventure;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.adventure.AdventureRepository;
import me.moirai.storyengine.infrastructure.outbound.adapter.favorite.FavoriteRepository;

@ExtendWith(MockitoExtension.class)
public class AddFavoriteAdventureHandlerTest {

    @Mock
    private AdventureRepository adventureQueryRepository;

    @Mock
    private FavoriteRepository favoriteRepository;

    @InjectMocks
    private AddFavoriteAdventureHandler handler;

    @Test
    public void addFavorite_whenValidAsset_thenCreateFavorite() {

        // Given
        Adventure adventure = AdventureFixture.privateMultiplayerAdventure().build();
        AddFavoriteAdventure command = AddFavoriteAdventure.builder()
                .assetId(adventure.getId())
                .playerId(adventure.getOwnerId())
                .build();

        when(adventureQueryRepository.findById(anyString())).thenReturn(Optional.of(adventure));

        // When
        handler.handle(command);

        // Then
        verify(favoriteRepository, times(1)).save(any());
    }

    @Test
    public void addFavorite_whenAssetNotFound_thenThrowException() {

        // Given
        AddFavoriteAdventure command = AddFavoriteAdventure.builder()
                .assetId("1234")
                .playerId("1234")
                .build();

        when(adventureQueryRepository.findById(anyString())).thenReturn(Optional.empty());

        // Then
        assertThrows(AssetNotFoundException.class, () -> handler.handle(command));
    }

    @Test
    public void addFavorite_whenNoViewingPermission_thenThrowException() {

        // Given
        String userId = "12345";
        Adventure unallowedAdventure = AdventureFixture.privateMultiplayerAdventure().build();
        AddFavoriteAdventure command = AddFavoriteAdventure.builder()
                .assetId(unallowedAdventure.getId())
                .playerId(userId)
                .build();

        when(adventureQueryRepository.findById(anyString())).thenReturn(Optional.of(unallowedAdventure));

        // Then
        assertThrows(AssetAccessDeniedException.class, () -> handler.handle(command));
    }
}
