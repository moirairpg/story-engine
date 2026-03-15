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
import org.springframework.test.util.ReflectionTestUtils;

import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.core.port.inbound.persona.GetPersonaById;
import me.moirai.storyengine.core.port.inbound.persona.PersonaDetails;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;
import me.moirai.storyengine.core.domain.PermissionsFixture;
import me.moirai.storyengine.core.domain.persona.Persona;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;

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

        when(repository.findByPublicId(anyString())).thenReturn(Optional.empty());

        // Then
        assertThrows(AssetNotFoundException.class, () -> handler.handle(query));
    }

    @Test
    public void getPersonaById_whenPersonaExists_thenReturnPersonaDetails() {

        // Given
        String publicId = "857345aa-0000-0000-0000-000000000000";
        String requesterId = "RQSTRID";
        Persona persona = PersonaFixture.privatePersona()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();
        ReflectionTestUtils.setField(persona, "id", 1L);
        ReflectionTestUtils.setField(persona, "publicId", publicId);

        GetPersonaById query = GetPersonaById.build(publicId, requesterId);

        when(repository.findByPublicId(anyString())).thenReturn(Optional.of(persona));

        // When
        PersonaDetails result = handler.handle(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(publicId);
    }

    @Test
    public void getPersonaById_whenAccessDenied_thenThrowException() {

        // Given
        String publicId = "857345aa-0000-0000-0000-000000000000";
        String requesterId = "RQSTRID";
        Persona persona = PersonaFixture.privatePersona().build();
        ReflectionTestUtils.setField(persona, "publicId", publicId);

        GetPersonaById query = GetPersonaById.build(publicId, requesterId);

        when(repository.findByPublicId(anyString())).thenReturn(Optional.of(persona));

        // When
        assertThrows(AssetAccessDeniedException.class, () -> handler.handle(query));
    }
}
