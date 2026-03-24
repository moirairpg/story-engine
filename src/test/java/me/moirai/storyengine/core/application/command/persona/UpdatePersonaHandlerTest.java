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
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.core.domain.persona.Persona;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.port.inbound.persona.UpdatePersona;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;

@ExtendWith(MockitoExtension.class)
class UpdatePersonaHandlerTest {

    @Mock
    private PersonaRepository repository;

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
                null, null, null, null, null,
                null, null, null, null);

        // then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    void shouldThrowExceptionWhenPersonaNotFound() {

        // given
        var personaId = PersonaFixture.PUBLIC_ID;
        var command = new UpdatePersona(
                personaId, "MoirAI", "I am a Discord chatbot",
                Visibility.PUBLIC, null,
                Set.of(123456L), Set.of(123456L),
                Set.of(123456L), Set.of(123456L));

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(AssetNotFoundException.class)
                .hasMessage("Persona was not found");
    }

    @Test
    void shouldUpdatePersonaWhenValidData() {

        // given
        var requesterId = "RQSTRID";
        var command = new UpdatePersona(
                PersonaFixture.PUBLIC_ID, "MoirAI", "I am a Discord chatbot",
                Visibility.PUBLIC, requesterId,
                Set.of(123456L), Set.of(123456L),
                Set.of(123456L), Set.of(123456L));

        var unchangedPersona = PersonaFixture.privatePersona().build();
        var expectedUpdatedPersona = PersonaFixture.privatePersona().name("New name").build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(unchangedPersona));
        when(repository.save(any(Persona.class))).thenReturn(expectedUpdatedPersona);

        // when
        var result = handler.handle(command);

        // then
        assertThat(result).isNotNull();
    }

    @Test
    void shouldUpdatePersonaWhenNoWriterUsersAreAdded() {

        // given
        var requesterId = "RQSTRID";
        var command = new UpdatePersona(
                PersonaFixture.PUBLIC_ID, "MoirAI", "I am a Discord chatbot",
                Visibility.PUBLIC, requesterId,
                null, Set.of(4567L),
                Set.of(123456L), Set.of(4567L));

        var unchangedPersona = PersonaFixture.privatePersona().build();
        var expectedUpdatedPersona = PersonaFixture.privatePersona().name("New name").build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(unchangedPersona));
        when(repository.save(any(Persona.class))).thenReturn(expectedUpdatedPersona);

        // when
        var result = handler.handle(command);

        // then
        assertThat(result).isNotNull();
    }

    @Test
    void shouldUpdatePersonaWhenNoReaderUsersAreAdded() {

        // given
        var requesterId = "RQSTRID";
        var command = new UpdatePersona(
                PersonaFixture.PUBLIC_ID, "MoirAI", "I am a Discord chatbot",
                Visibility.PUBLIC, requesterId,
                Set.of(123456L), Set.of(4567L),
                null, Set.of(4567L));

        var unchangedPersona = PersonaFixture.privatePersona().build();
        var expectedUpdatedPersona = PersonaFixture.privatePersona().name("New name").build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(unchangedPersona));
        when(repository.save(any(Persona.class))).thenReturn(expectedUpdatedPersona);

        // when
        var result = handler.handle(command);

        // then
        assertThat(result).isNotNull();
    }

    @Test
    void shouldUpdatePersonaWhenNoReaderUsersAreRemoved() {

        // given
        var requesterId = "RQSTRID";
        var command = new UpdatePersona(
                PersonaFixture.PUBLIC_ID, "MoirAI", "I am a Discord chatbot",
                Visibility.PUBLIC, requesterId,
                Set.of(123456L), Set.of(123456L),
                Set.of(123456L), null);

        var unchangedPersona = PersonaFixture.privatePersona().build();
        var expectedUpdatedPersona = PersonaFixture.privatePersona().name("New name").build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(unchangedPersona));
        when(repository.save(any(Persona.class))).thenReturn(expectedUpdatedPersona);

        // when
        var result = handler.handle(command);

        // then
        assertThat(result).isNotNull();
    }

    @Test
    void shouldUpdatePersonaWhenNoWriterUsersAreRemoved() {

        // given
        var requesterId = "RQSTRID";
        var command = new UpdatePersona(
                PersonaFixture.PUBLIC_ID, "MoirAI", "I am a Discord chatbot",
                Visibility.PUBLIC, requesterId,
                Set.of(123456L), null,
                Set.of(123456L), Set.of(4567L));

        var unchangedPersona = PersonaFixture.privatePersona().build();
        var expectedUpdatedPersona = PersonaFixture.privatePersona().name("New name").build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(unchangedPersona));
        when(repository.save(any(Persona.class))).thenReturn(expectedUpdatedPersona);

        // when
        var result = handler.handle(command);

        // then
        assertThat(result).isNotNull();
    }

    @Test
    void shouldMakePersonaPrivateWhenVisibilityChangedToPrivate() {

        // given
        var requesterId = "RQSTRID";
        var command = new UpdatePersona(
                PersonaFixture.PUBLIC_ID, null, null,
                Visibility.PRIVATE, requesterId,
                null, null, null, null);

        var unchangedPersona = PersonaFixture.publicPersona().build();
        var expectedUpdatedPersona = PersonaFixture.privatePersona().build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(unchangedPersona));
        when(repository.save(any(Persona.class))).thenReturn(expectedUpdatedPersona);

        // when
        var result = handler.handle(command);

        // then
        assertThat(result).isNotNull();
    }

    @Test
    void shouldNotChangePersonaWhenNullVisibility() {

        // given
        var requesterId = "RQSTRID";
        var command = new UpdatePersona(
                PersonaFixture.PUBLIC_ID, null, null,
                null, requesterId,
                null, null, null, null);

        var unchangedPersona = PersonaFixture.privatePersona().build();
        var expectedUpdatedPersona = PersonaFixture.privatePersona().build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(unchangedPersona));
        when(repository.save(any(Persona.class))).thenReturn(expectedUpdatedPersona);

        // when
        var result = handler.handle(command);

        // then
        assertThat(result).isNotNull();
    }

    @Test
    void shouldNotChangePersonaWhenAllFieldsAreNull() {

        // given
        var requesterId = "RQSTRID";
        var command = new UpdatePersona(
                PersonaFixture.PUBLIC_ID, null, null,
                null, requesterId,
                null, null, null, null);

        var unchangedPersona = PersonaFixture.privatePersona().build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(unchangedPersona));
        when(repository.save(any(Persona.class))).thenReturn(unchangedPersona);

        // when
        var result = handler.handle(command);

        // then
        assertThat(result).isNotNull();
    }
}
