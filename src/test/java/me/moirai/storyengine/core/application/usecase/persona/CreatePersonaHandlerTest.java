package me.moirai.storyengine.core.application.usecase.persona;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import me.moirai.storyengine.core.application.usecase.persona.request.CreatePersonaFixture;
import me.moirai.storyengine.core.domain.persona.Persona;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.port.inbound.persona.CreatePersona;
import me.moirai.storyengine.core.port.outbound.generation.TextModerationPort;
import me.moirai.storyengine.core.port.outbound.generation.TextModerationResult;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class CreatePersonaHandlerTest {

    @Mock
    private TextModerationPort moderationPort;

    @Mock
    private PersonaRepository repository;

    @InjectMocks
    private CreatePersonaHandler handler;

    @Test
    public void errorWhenCommandIsNull() {

        // Given
        CreatePersona command = null;

        // Then
        assertThrows(IllegalArgumentException.class, () -> handler.handle(command));
    }

    @Test
    public void createPersona() {

        // Given
        Persona persona = PersonaFixture.privatePersona().build();

        CreatePersona command = CreatePersonaFixture.createPrivatePersona();

        TextModerationResult moderationResult = TextModerationResult.builder()
                .contentFlagged(false)
                .build();

        when(moderationPort.moderate(anyString())).thenReturn(Mono.just(moderationResult));
        when(repository.save(any(Persona.class))).thenReturn(persona);

        // Then
        StepVerifier.create(handler.handle(command))
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result.name()).isEqualTo(persona.getName());
                    assertThat(result.personality()).isEqualTo(persona.getPersonality());
                    assertThat(result.visibility()).isEqualTo(persona.getVisibility());
                    assertThat(result.ownerId()).isEqualTo(persona.getOwnerId());
                    assertThat(result.usersAllowedToRead()).isEqualTo(persona.getUsersAllowedToRead());
                    assertThat(result.usersAllowedToWrite()).isEqualTo(persona.getUsersAllowedToWrite());
                })
                .verifyComplete();
    }
}
