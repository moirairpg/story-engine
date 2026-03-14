package me.moirai.storyengine.core.application.usecase.persona;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.core.application.usecase.persona.request.CreatePersona;
import me.moirai.storyengine.core.application.usecase.persona.request.CreatePersonaFixture;
import me.moirai.storyengine.core.domain.persona.Persona;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.domain.persona.PersonaService;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class CreatePersonaHandlerTest {

    @Mock
    private PersonaService domainService;

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
        String id = "HAUDHUAHD";
        Persona persona = PersonaFixture.privatePersona().id(id).build();
        CreatePersona command = CreatePersonaFixture.createPrivatePersona().build();

        when(domainService.createFrom(any(CreatePersona.class)))
                .thenReturn(Mono.just(persona));

        // Then
        StepVerifier.create(handler.handle(command))
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result.getId()).isEqualTo(id);
                })
                .verifyComplete();
    }
}
