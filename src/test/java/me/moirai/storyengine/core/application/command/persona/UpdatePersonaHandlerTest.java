package me.moirai.storyengine.core.application.command.persona;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.util.Sets.set;
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

import me.moirai.storyengine.common.enums.Visibility;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.core.domain.PermissionsFixture;
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

        UpdatePersona command = null;

        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    void shouldThrowExceptionWhenIdIsNull() {

        var command = new UpdatePersona(
                null, null, null, null, null,
                null, null, null, null);

        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    void shouldThrowExceptionWhenPersonaNotFound() {

        var personaId = PersonaFixture.PUBLIC_ID;
        var command = new UpdatePersona(
                personaId, "MoirAI", "I am a Discord chatbot",
                Visibility.PUBLIC, null,
                set("123456"), set("123456"),
                set("123456"), set("123456"));

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(AssetNotFoundException.class)
                .hasMessage("Persona was not found");
    }

    @Test
    void shouldUpdatePersonaWhenValidData() {

        var requesterId = "RQSTRID";
        var command = new UpdatePersona(
                PersonaFixture.PUBLIC_ID, "MoirAI", "I am a Discord chatbot",
                Visibility.PUBLIC, requesterId,
                set("123456"), set("123456"),
                set("123456"), set("123456"));

        var unchangedPersona = PersonaFixture.privatePersona()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        var expectedUpdatedPersona = PersonaFixture.privatePersona()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .name("New name")
                .build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(unchangedPersona));
        when(repository.save(any(Persona.class))).thenReturn(expectedUpdatedPersona);

        var result = handler.handle(command);

        assertThat(result).isNotNull();
    }

    @Test
    void shouldUpdatePersonaWhenNoWriterUsersAreAdded() {

        var requesterId = "RQSTRID";
        var command = new UpdatePersona(
                PersonaFixture.PUBLIC_ID, "MoirAI", "I am a Discord chatbot",
                Visibility.PUBLIC, requesterId,
                null, set("4567"),
                set("123456"), set("4567"));

        var unchangedPersona = PersonaFixture.privatePersona()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        var expectedUpdatedPersona = PersonaFixture.privatePersona()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .name("New name")
                .build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(unchangedPersona));
        when(repository.save(any(Persona.class))).thenReturn(expectedUpdatedPersona);

        var result = handler.handle(command);

        assertThat(result).isNotNull();
    }

    @Test
    void shouldUpdatePersonaWhenNoReaderUsersAreAdded() {

        var requesterId = "RQSTRID";
        var command = new UpdatePersona(
                PersonaFixture.PUBLIC_ID, "MoirAI", "I am a Discord chatbot",
                Visibility.PUBLIC, requesterId,
                set("123456"), set("4567"),
                null, set("4567"));

        var unchangedPersona = PersonaFixture.privatePersona()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        var expectedUpdatedPersona = PersonaFixture.privatePersona()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .name("New name")
                .build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(unchangedPersona));
        when(repository.save(any(Persona.class))).thenReturn(expectedUpdatedPersona);

        var result = handler.handle(command);

        assertThat(result).isNotNull();
    }

    @Test
    void shouldUpdatePersonaWhenNoReaderUsersAreRemoved() {

        var requesterId = "RQSTRID";
        var command = new UpdatePersona(
                PersonaFixture.PUBLIC_ID, "MoirAI", "I am a Discord chatbot",
                Visibility.PUBLIC, requesterId,
                set("123456"), set("123456"),
                set("123456"), null);

        var unchangedPersona = PersonaFixture.privatePersona()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        var expectedUpdatedPersona = PersonaFixture.privatePersona()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .name("New name")
                .build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(unchangedPersona));
        when(repository.save(any(Persona.class))).thenReturn(expectedUpdatedPersona);

        var result = handler.handle(command);

        assertThat(result).isNotNull();
    }

    @Test
    void shouldUpdatePersonaWhenNoWriterUsersAreRemoved() {

        var requesterId = "RQSTRID";
        var command = new UpdatePersona(
                PersonaFixture.PUBLIC_ID, "MoirAI", "I am a Discord chatbot",
                Visibility.PUBLIC, requesterId,
                set("123456"), null,
                set("123456"), set("4567"));

        var unchangedPersona = PersonaFixture.privatePersona()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        var expectedUpdatedPersona = PersonaFixture.privatePersona()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .name("New name")
                .build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(unchangedPersona));
        when(repository.save(any(Persona.class))).thenReturn(expectedUpdatedPersona);

        var result = handler.handle(command);

        assertThat(result).isNotNull();
    }

    @Test
    void shouldMakePersonaPrivateWhenVisibilityChangedToPrivate() {

        var requesterId = "RQSTRID";
        var command = new UpdatePersona(
                PersonaFixture.PUBLIC_ID, null, null,
                Visibility.PRIVATE, requesterId,
                null, null, null, null);

        var unchangedPersona = PersonaFixture.publicPersona()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        var expectedUpdatedPersona = PersonaFixture.privatePersona()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(unchangedPersona));
        when(repository.save(any(Persona.class))).thenReturn(expectedUpdatedPersona);

        var result = handler.handle(command);

        assertThat(result).isNotNull();
    }

    @Test
    void shouldNotChangePersonaWhenNullVisibility() {

        var requesterId = "RQSTRID";
        var command = new UpdatePersona(
                PersonaFixture.PUBLIC_ID, null, null,
                null, requesterId,
                null, null, null, null);

        var unchangedPersona = PersonaFixture.privatePersona()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        var expectedUpdatedPersona = PersonaFixture.privatePersona().build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(unchangedPersona));
        when(repository.save(any(Persona.class))).thenReturn(expectedUpdatedPersona);

        var result = handler.handle(command);

        assertThat(result).isNotNull();
    }

    @Test
    void shouldNotChangePersonaWhenAllFieldsAreNull() {

        var requesterId = "RQSTRID";
        var command = new UpdatePersona(
                PersonaFixture.PUBLIC_ID, null, null,
                null, requesterId,
                null, null, null, null);

        var unchangedPersona = PersonaFixture.privatePersona()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(unchangedPersona));
        when(repository.save(any(Persona.class))).thenReturn(unchangedPersona);

        var result = handler.handle(command);

        assertThat(result).isNotNull();
    }
}
