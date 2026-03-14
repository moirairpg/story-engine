package me.moirai.storyengine.core.application.usecase.persona;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.core.port.inbound.persona.GetPersonaById;
import me.moirai.storyengine.core.port.inbound.persona.GetPersonaResult;
import me.moirai.storyengine.core.domain.PermissionsFixture;
import me.moirai.storyengine.core.domain.persona.Persona;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.domain.persona.PersonaRepository;

@ExtendWith(MockitoExtension.class)
public class GetPersonaByIdHandlerTest {

    @Mock
    private PersonaRepository repository;

    @InjectMocks
    private GetPersonaByIdHandler handler;

    @Test
    public void getPersonaById_whenIdIsNull_thenThrowException() {

        // Given
        String requesterId = "RQSTRID";
        GetPersonaById query = GetPersonaById.build(null, requesterId);

        // Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(query));
    }

    @Test
    public void getPersonaById_whenPersonaNotFound_thenThrowException() {

        // Given
        String id = "HAUDHUAHD";
        String requesterId = "RQSTRID";
        GetPersonaById query = GetPersonaById.build(id, requesterId);

        when(repository.findById(anyString())).thenReturn(Optional.empty());

        // Then
        assertThrows(AssetNotFoundException.class, () -> handler.handle(query));
    }

    @Test
    public void getPersonaById_whenPersonaExists_thenReturnPersonaDetails() {

        // Given
        String id = "HAUDHUAHD";
        String requesterId = "RQSTRID";
        Persona persona = PersonaFixture.privatePersona()
                .id(id)
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        GetPersonaById query = GetPersonaById.build(id, requesterId);

        when(repository.findById(anyString())).thenReturn(Optional.of(persona));

        // When
        GetPersonaResult result = handler.handle(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
    }

    @Test
    public void getPersonaById_whenAccessDenied_thenThrowException() {

        // Given
        String id = "HAUDHUAHD";
        String requesterId = "RQSTRID";
        Persona persona = PersonaFixture.privatePersona()
                .id(id)
                .build();

        GetPersonaById query = GetPersonaById.build(id, requesterId);

        when(repository.findById(anyString())).thenReturn(Optional.of(persona));

        // When
        assertThrows(AssetAccessDeniedException.class, () -> handler.handle(query));
    }
}