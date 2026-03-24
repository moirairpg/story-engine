package me.moirai.storyengine.core.application.query.persona;

import static org.assertj.core.api.Assertions.assertThat;
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

import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.port.inbound.persona.GetPersonaById;
import me.moirai.storyengine.core.port.inbound.persona.PersonaDetails;
import me.moirai.storyengine.core.port.outbound.persona.PersonaReader;

@ExtendWith(MockitoExtension.class)
public class GetPersonaByIdHandlerTest {

    @Mock
    private PersonaReader reader;

    @InjectMocks
    private GetPersonaByIdHandler handler;

    @Test
    public void getPersonaById_whenIdIsNull_thenThrowException() {

        // Given
        var query = new GetPersonaById(null, "RQSTRID");

        // Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(query));
    }

    @Test
    public void getPersonaById_whenPersonaNotFound_thenThrowException() {

        // Given
        var query = new GetPersonaById(PersonaFixture.PUBLIC_ID, "RQSTRID");

        when(reader.getPersonaById(any(UUID.class))).thenReturn(Optional.empty());

        // Then
        assertThrows(AssetNotFoundException.class, () -> handler.handle(query));
    }

    @Test
    public void getPersonaById_whenPersonaExists_thenReturnPersonaDetails() {

        // Given
        var expectedDetails = new PersonaDetails(
                PersonaFixture.PUBLIC_ID, "MoirAI", "I am a bot", null,
                java.util.Set.of(),
                null, null);

        var query = new GetPersonaById(PersonaFixture.PUBLIC_ID, "RQSTRID");

        when(reader.getPersonaById(any(UUID.class))).thenReturn(Optional.of(expectedDetails));

        // When
        var result = handler.handle(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(PersonaFixture.PUBLIC_ID);
    }
}
