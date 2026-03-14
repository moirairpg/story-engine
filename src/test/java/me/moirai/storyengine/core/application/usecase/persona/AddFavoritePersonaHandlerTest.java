package me.moirai.storyengine.core.application.usecase.persona;

import static org.junit.jupiter.api.Assertions.assertThrows;
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
import me.moirai.storyengine.core.application.usecase.persona.request.AddFavoritePersona;
import me.moirai.storyengine.core.domain.persona.Persona;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.domain.persona.PersonaRepository;
import me.moirai.storyengine.infrastructure.outbound.adapter.favorite.FavoriteRepository;

@ExtendWith(MockitoExtension.class)
public class AddFavoritePersonaHandlerTest {

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private FavoriteRepository favoriteRepository;

    @InjectMocks
    private AddFavoritePersonaHandler handler;

    @Test
    public void addFavorite_whenValidAsset_thenCreateFavorite() {

        // Given
        AddFavoritePersona command = AddFavoritePersona.builder()
                .assetId("1234")
                .playerId("1234")
                .build();

        Persona persona = PersonaFixture.publicPersona().build();

        when(personaRepository.findById(anyString())).thenReturn(Optional.of(persona));

        // When
        handler.handle(command);

        // Then
        verify(favoriteRepository, times(1)).save(any());
    }

    @Test
    public void addFavorite_whenAssetNotFound_thenThrowException() {

        // Given
        AddFavoritePersona command = AddFavoritePersona.builder()
                .assetId("1234")
                .playerId("1234")
                .build();

        when(personaRepository.findById(anyString())).thenReturn(Optional.empty());

        // Then
        assertThrows(AssetNotFoundException.class, () -> handler.handle(command));
    }

    @Test
    public void addFavorite_whenAccessDenied_thenThrowException() {

        // Given
        AddFavoritePersona command = AddFavoritePersona.builder()
                .assetId("1234")
                .playerId("INVLDUSR")
                .build();

        Persona persona = PersonaFixture.privatePersona().build();

        when(personaRepository.findById(anyString())).thenReturn(Optional.of(persona));

        // Then
        assertThrows(AssetAccessDeniedException.class, () -> handler.handle(command));
    }
}
