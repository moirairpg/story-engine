package me.moirai.storyengine.core.application.command.persona;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
import me.moirai.storyengine.core.port.inbound.persona.DeletePersona;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;

@ExtendWith(MockitoExtension.class)
public class DeletePersonaHandlerTest {

    @Mock
    private PersonaRepository repository;

    @InjectMocks
    private DeletePersonaHandler handler;

    @Test
    public void deletePersona_whenIdIsNull_thenThrowException() {

        // given
        UUID id = null;
        var command = new DeletePersona(id);

        // then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> handler.handle(command));
    }

    @Test
    public void deletePersona_whenPersonaNotFound_thenThrowException() {

        // given
        var id = PersonaFixture.PUBLIC_ID;
        var command = new DeletePersona(id);

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

        // then
        assertThatExceptionOfType(AssetNotFoundException.class)
                .isThrownBy(() -> handler.handle(command));
    }

    @Test
    public void deletePersona_whenProperIdAndPermission_thenPersonaIsDeleted() {

        // given
        var id = PersonaFixture.PUBLIC_ID;
        var command = new DeletePersona(id);

        var persona = PersonaFixture.privatePersona().name("New name").build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(persona));

        // when
        handler.handle(command);

        // then
        verify(repository, times(1)).deleteByPublicId(any(UUID.class));
    }
}
