package me.moirai.storyengine.core.application.command.persona;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.core.domain.persona.Persona;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.port.inbound.CreatePersonaFixture;
import me.moirai.storyengine.core.port.inbound.persona.CreatePersona;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;

@ExtendWith(MockitoExtension.class)
class CreatePersonaHandlerTest {

    @Mock
    private PersonaRepository repository;

    @InjectMocks
    private CreatePersonaHandler handler;

    @Test
    void shouldThrowExceptionWhenCommandIsNull() {

        CreatePersona command = null;

        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    void shouldCreatePersona() {

        var persona = PersonaFixture.privatePersonaWithId();
        var command = CreatePersonaFixture.createPrivatePersona();

        when(repository.save(any(Persona.class))).thenReturn(persona);

        var result = handler.handle(command);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo(persona.getName());
        assertThat(result.personality()).isEqualTo(persona.getPersonality());
        assertThat(result.visibility()).isEqualTo(persona.getVisibility());
        assertThat(result.ownerId()).isEqualTo(persona.getOwnerId());
        assertThat(result.usersAllowedToRead()).isEqualTo(persona.getUsersAllowedToRead());
        assertThat(result.usersAllowedToWrite()).isEqualTo(persona.getUsersAllowedToWrite());
    }
}
