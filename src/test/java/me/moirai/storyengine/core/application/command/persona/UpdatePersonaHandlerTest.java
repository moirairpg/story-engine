package me.moirai.storyengine.core.application.command.persona;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Sets.set;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.enums.Visibility;
import me.moirai.storyengine.common.exception.AssetAccessDeniedException;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.exception.ModerationException;
import me.moirai.storyengine.core.domain.PermissionsFixture;
import me.moirai.storyengine.core.domain.persona.Persona;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.port.inbound.persona.UpdatePersona;
import me.moirai.storyengine.core.port.outbound.generation.TextModerationPort;
import me.moirai.storyengine.core.port.outbound.generation.TextModerationResultFixture;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class UpdatePersonaHandlerTest {

    @Mock
    private PersonaRepository repository;

    @Mock
    private TextModerationPort moderationPort;

    @InjectMocks
    private UpdatePersonaHandler handler;

    @Test
    public void updatePersona_whenPersonaNotFound_thenThrowException() {

        // Given
        UUID id = PersonaFixture.PUBLIC_ID;
        UpdatePersona command = new UpdatePersona(
                id,
                "MoirAI",
                "I am a Discord chatbot",
                Visibility.PUBLIC,
                null,
                set("123456"),
                set("123456"),
                set("123456"),
                set("123456"));

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());
        when(moderationPort.moderate(anyString()))
                .thenReturn(Mono.just(TextModerationResultFixture.withoutFlags().build()));

        // Then
        StepVerifier.create(handler.handle(command))
                .verifyErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(AssetNotFoundException.class);
                    assertThat(error).message().isEqualTo("Persona was not found");
                });
    }

    @Test
    public void updatePersona_whenNotAuthorized_thenThrowException() {

        // Given
        UUID id = PersonaFixture.PUBLIC_ID;
        String requesterId = "RQSTRID";
        UpdatePersona command = new UpdatePersona(
                id,
                "MoirAI",
                "I am a Discord chatbot",
                Visibility.PUBLIC,
                requesterId,
                set("123456"),
                set("123456"),
                set("123456"),
                set("123456"));

        Persona unchangedPersona = PersonaFixture.privatePersona()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId("SOMEOTHER")
                        .build())
                .build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(unchangedPersona));
        when(moderationPort.moderate(anyString()))
                .thenReturn(Mono.just(TextModerationResultFixture.withoutFlags().build()));

        // Then
        StepVerifier.create(handler.handle(command))
                .verifyErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(AssetAccessDeniedException.class);
                    assertThat(error).message().isEqualTo("User does not have permission to modify the persona");
                });
    }

    @Test
    public void updatePersona_whenValidData_thenPersonaIsUpdated() {

        // Given
        UUID id = PersonaFixture.PUBLIC_ID;
        String requesterId = "RQSTRID";
        UpdatePersona command = new UpdatePersona(
                id,
                "MoirAI",
                "I am a Discord chatbot",
                Visibility.PUBLIC,
                requesterId,
                set("123456"),
                set("123456"),
                set("123456"),
                set("123456"));

        Persona unchangedPersona = PersonaFixture.privatePersona()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        Persona expectedUpdatedPersona = PersonaFixture.privatePersona()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .name("New name")
                .build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(unchangedPersona));
        when(repository.save(any(Persona.class))).thenReturn(expectedUpdatedPersona);
        when(moderationPort.moderate(anyString()))
                .thenReturn(Mono.just(TextModerationResultFixture.withoutFlags().build()));

        // Then
        StepVerifier.create(handler.handle(command))
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    public void updatePersona_whenNoWriterUsersAreAdded_thenPersonaIsUpdated() {

        // Given
        UUID id = PersonaFixture.PUBLIC_ID;
        String requesterId = "RQSTRID";
        UpdatePersona command = new UpdatePersona(
                id,
                "MoirAI",
                "I am a Discord chatbot",
                Visibility.PUBLIC,
                requesterId,
                null,
                set("4567"),
                set("123456"),
                set("4567"));

        Persona unchangedPersona = PersonaFixture.privatePersona()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        Persona expectedUpdatedPersona = PersonaFixture.privatePersona()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .name("New name")
                .build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(unchangedPersona));
        when(repository.save(any(Persona.class))).thenReturn(expectedUpdatedPersona);
        when(moderationPort.moderate(anyString()))
                .thenReturn(Mono.just(TextModerationResultFixture.withoutFlags().build()));

        // Then
        StepVerifier.create(handler.handle(command))
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    public void updatePersona_whenNoReaderUsersAreAdded_thenPersonaIsUpdated() {

        // Given
        UUID id = PersonaFixture.PUBLIC_ID;
        String requesterId = "RQSTRID";
        UpdatePersona command = new UpdatePersona(
                id,
                "MoirAI",
                "I am a Discord chatbot",
                Visibility.PUBLIC,
                requesterId,
                set("123456"),
                set("4567"),
                null,
                set("4567"));

        Persona unchangedPersona = PersonaFixture.privatePersona()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        Persona expectedUpdatedPersona = PersonaFixture.privatePersona()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .name("New name")
                .build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(unchangedPersona));
        when(repository.save(any(Persona.class))).thenReturn(expectedUpdatedPersona);
        when(moderationPort.moderate(anyString()))
                .thenReturn(Mono.just(TextModerationResultFixture.withoutFlags().build()));

        // Then
        StepVerifier.create(handler.handle(command))
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    public void updatePersona_whenNoReaderUsersAreRemoved_thenPersonaIsUpdated() {

        // Given
        UUID id = PersonaFixture.PUBLIC_ID;
        String requesterId = "RQSTRID";
        UpdatePersona command = new UpdatePersona(
                id,
                "MoirAI",
                "I am a Discord chatbot",
                Visibility.PUBLIC,
                requesterId,
                set("123456"),
                set("123456"),
                set("123456"),
                null);

        Persona unchangedPersona = PersonaFixture.privatePersona()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        Persona expectedUpdatedPersona = PersonaFixture.privatePersona()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .name("New name")
                .build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(unchangedPersona));
        when(repository.save(any(Persona.class))).thenReturn(expectedUpdatedPersona);
        when(moderationPort.moderate(anyString()))
                .thenReturn(Mono.just(TextModerationResultFixture.withoutFlags().build()));

        // Then
        StepVerifier.create(handler.handle(command))
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    public void updatePersona_whenNoWriterUsersAreRemoved_thenPersonaIsUpdated() {

        // Given
        UUID id = PersonaFixture.PUBLIC_ID;
        String requesterId = "RQSTRID";
        UpdatePersona command = new UpdatePersona(
                id,
                "MoirAI",
                "I am a Discord chatbot",
                Visibility.PUBLIC,
                requesterId,
                set("123456"),
                null,
                set("123456"),
                set("4567"));

        Persona unchangedPersona = PersonaFixture.privatePersona()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        Persona expectedUpdatedPersona = PersonaFixture.privatePersona()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .name("New name")
                .build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(unchangedPersona));
        when(repository.save(any(Persona.class))).thenReturn(expectedUpdatedPersona);
        when(moderationPort.moderate(anyString()))
                .thenReturn(Mono.just(TextModerationResultFixture.withoutFlags().build()));

        // Then
        StepVerifier.create(handler.handle(command))
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    public void updatePersona_whenPublicToMakePrivate_thenPersonaIsMadePrivate() {

        // Given
        UUID id = PersonaFixture.PUBLIC_ID;
        String requesterId = "RQSTRID";
        UpdatePersona command = new UpdatePersona(
                id,
                null,
                null,
                Visibility.PRIVATE,
                requesterId,
                null,
                null,
                null,
                null);

        Persona unchangedPersona = PersonaFixture.publicPersona()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        Persona expectedUpdatedPersona = PersonaFixture.privatePersona()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(unchangedPersona));
        when(repository.save(any(Persona.class))).thenReturn(expectedUpdatedPersona);

        // Then
        StepVerifier.create(handler.handle(command))
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    public void updatePersona_whenNullVisibility_thenNothingIsChanged() {

        // Given
        UUID id = PersonaFixture.PUBLIC_ID;
        String requesterId = "RQSTRID";
        UpdatePersona command = new UpdatePersona(
                id,
                null,
                null,
                null,
                requesterId,
                null,
                null,
                null,
                null);

        Persona unchangedPersona = PersonaFixture.privatePersona()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        Persona expectedUpdatedPersona = PersonaFixture.privatePersona().build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(unchangedPersona));
        when(repository.save(any(Persona.class))).thenReturn(expectedUpdatedPersona);

        // Then
        StepVerifier.create(handler.handle(command))
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    public void updatePersona_whenContentIsFlagged_thenThrowException() {

        // Given
        UUID id = PersonaFixture.PUBLIC_ID;
        UpdatePersona command = new UpdatePersona(
                id,
                "MoirAI",
                "I am a Discord chatbot",
                Visibility.PUBLIC,
                null,
                null,
                null,
                null,
                null);

        when(moderationPort.moderate(anyString()))
                .thenReturn(Mono.just(TextModerationResultFixture.withFlags().build()));

        // Then
        StepVerifier.create(handler.handle(command))
                .verifyError(ModerationException.class);
    }

    @Test
    public void updatePersona_whenUpdateFieldsAreEmpty_thenPersonaIsNotChanged() {

        // Given
        UUID id = PersonaFixture.PUBLIC_ID;
        String requesterId = "RQSTRID";
        UpdatePersona command = new UpdatePersona(
                id,
                null,
                null,
                null,
                requesterId,
                null,
                null,
                null,
                null);

        Persona unchangedPersona = PersonaFixture.privatePersona()
                .permissions(PermissionsFixture.samplePermissions()
                        .ownerId(requesterId)
                        .build())
                .build();

        when(repository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(unchangedPersona));
        when(repository.save(any(Persona.class))).thenReturn(unchangedPersona);

        // Then
        StepVerifier.create(handler.handle(command))
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    public void errorWhenIdIsNull() {

        // Given
        UUID id = null;
        UpdatePersona command = new UpdatePersona(
                id,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        // Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }
}
