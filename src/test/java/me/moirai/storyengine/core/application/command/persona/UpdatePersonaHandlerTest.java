package me.moirai.storyengine.core.application.command.persona;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.enums.Visibility;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.domain.persona.Persona;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.port.inbound.persona.UpdatePersona;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;
import me.moirai.storyengine.core.domain.userdetails.UserFixture;

@ExtendWith(MockitoExtension.class)
class UpdatePersonaHandlerTest {

    @Mock
    private PersonaRepository repository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UpdatePersonaHandler handler;

    @Test
    void shouldThrowExceptionWhenCommandIsNull() {

        // given
        UpdatePersona command = null;

        // then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    void shouldThrowExceptionWhenIdIsNull() {

        // given
        var command = new UpdatePersona(
                null, null, null, null, null);

        // then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    void shouldThrowExceptionWhenPersonaNotFound() {

        // given
        var personaId = PersonaFixture.PUBLIC_ID;
        var command = new UpdatePersona(
                personaId, "MoirAI", "I am a Discord chatbot",
                Visibility.PUBLIC,
                Set.of());

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Persona was not found");
    }

    @Test
    void shouldUpdatePersonaWhenValidData() {

        // given
        var command = new UpdatePersona(
                PersonaFixture.PUBLIC_ID, "MoirAI", "I am a Discord chatbot",
                Visibility.PUBLIC,
                Set.of());

        var unchangedPersona = PersonaFixture.privatePersona().build();
        var expectedUpdatedPersona = PersonaFixture.privatePersona().name("New name").build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(unchangedPersona));
        when(repository.save(any(Persona.class))).thenReturn(expectedUpdatedPersona);
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(UserFixture.playerWithId()));

        // when
        var result = handler.handle(command);

        // then
        assertThat(result).isNotNull();
    }

    @Test
    void shouldMakePersonaPrivateWhenVisibilityChangedToPrivate() {

        // given
        var command = new UpdatePersona(
                PersonaFixture.PUBLIC_ID, null, null,
                Visibility.PRIVATE,
                null);

        var unchangedPersona = PersonaFixture.publicPersona().build();
        var expectedUpdatedPersona = PersonaFixture.privatePersona().build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(unchangedPersona));
        when(repository.save(any(Persona.class))).thenReturn(expectedUpdatedPersona);
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(UserFixture.playerWithId()));

        // when
        var result = handler.handle(command);

        // then
        assertThat(result).isNotNull();
    }

    @Test
    void shouldNotChangePersonaWhenNullVisibility() {

        // given
        var command = new UpdatePersona(
                PersonaFixture.PUBLIC_ID, null, null,
                null,
                null);

        var unchangedPersona = PersonaFixture.privatePersona().build();
        var expectedUpdatedPersona = PersonaFixture.privatePersona().build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(unchangedPersona));
        when(repository.save(any(Persona.class))).thenReturn(expectedUpdatedPersona);
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(UserFixture.playerWithId()));

        // when
        var result = handler.handle(command);

        // then
        assertThat(result).isNotNull();
    }

    @Test
    void shouldNotChangePersonaWhenAllFieldsAreNull() {

        // given
        var command = new UpdatePersona(
                PersonaFixture.PUBLIC_ID, null, null,
                null,
                null);

        var unchangedPersona = PersonaFixture.privatePersona().build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(unchangedPersona));
        when(repository.save(any(Persona.class))).thenReturn(unchangedPersona);
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(UserFixture.playerWithId()));

        // when
        var result = handler.handle(command);

        // then
        assertThat(result).isNotNull();
    }
}
