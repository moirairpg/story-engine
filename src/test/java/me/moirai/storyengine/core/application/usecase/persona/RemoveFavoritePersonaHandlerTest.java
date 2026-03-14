package me.moirai.storyengine.core.application.usecase.persona;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.core.application.usecase.persona.request.RemoveFavoritePersona;
import me.moirai.storyengine.infrastructure.outbound.adapter.favorite.FavoriteRepository;

@ExtendWith(MockitoExtension.class)
public class RemoveFavoritePersonaHandlerTest {

    @Mock
    private FavoriteRepository repository;

    @InjectMocks
    private RemoveFavoritePersonaHandler handler;

    @Test
    public void removeFavorite_whenValidData_thenRemove() {

        // Given
        String assetId = "1234";
        String userId = "1234";

        RemoveFavoritePersona request = RemoveFavoritePersona.builder()
                .assetId(assetId)
                .playerId(userId)
                .build();

        // When
        handler.handle(request);

        // Then
        verify(repository, times(1))
                .deleteByPlayerIdAndAssetIdAndAssetType(anyString(), anyString(), anyString());
    }
}
