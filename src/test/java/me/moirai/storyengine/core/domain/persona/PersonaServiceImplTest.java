package me.moirai.storyengine.core.domain.persona;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.exception.ModerationException;
import me.moirai.storyengine.core.application.model.result.TextModerationResultFixture;
import me.moirai.storyengine.core.application.port.TextModerationPort;
import me.moirai.storyengine.core.application.usecase.persona.request.CreatePersona;
import me.moirai.storyengine.core.domain.Visibility;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class PersonaServiceImplTest {

    @Mock
    private TextModerationPort moderationPort;

    @Mock
    private PersonaRepository repository;

    @InjectMocks
    private PersonaServiceImpl service;

    @Test
    public void createPersona_whenValidData_thenPersonaIsCreatedSuccessfully() {

        // Given
        String name = "MoirAI";
        String personality = "I am a chatbot";
        String visibility = "PRIVATE";

        Persona expectedPersona = PersonaFixture.privatePersona()
                .name(name)
                .personality(personality)
                .visibility(Visibility.fromString(visibility))
                .build();

        CreatePersona command = CreatePersona.builder()
                .name(name)
                .personality(personality)
                .visibility(visibility)
                .build();

        when(moderationPort.moderate(anyString()))
                .thenReturn(Mono.just(TextModerationResultFixture.withoutFlags().build()));

        when(repository.save(any(Persona.class))).thenReturn(expectedPersona);

        // Then
        StepVerifier.create(service.createFrom(command))
                .assertNext(createdPersona -> {
                    assertThat(createdPersona).isNotNull().isEqualTo(expectedPersona);
                    assertThat(createdPersona.getName()).isEqualTo(expectedPersona.getName());
                    assertThat(createdPersona.getOwnerId()).isEqualTo(expectedPersona.getOwnerId());
                    assertThat(createdPersona.getUsersAllowedToWrite())
                            .isEqualTo(expectedPersona.getUsersAllowedToWrite());

                    assertThat(createdPersona.getUsersAllowedToRead())
                            .isEqualTo(expectedPersona.getUsersAllowedToRead());

                    assertThat(createdPersona.getPersonality()).isEqualTo(expectedPersona.getPersonality());
                    assertThat(createdPersona.getVisibility()).isEqualTo(expectedPersona.getVisibility());
                })
                .verifyComplete();
    }

    @Test
    public void createPersona_whenContentIsFlagged_thenThrowException() {

        // Given
        String name = "MoirAI";
        String personality = "I am a chatbot";
        String visibility = "PRIVATE";

        CreatePersona command = CreatePersona.builder()
                .name(name)
                .personality(personality)
                .visibility(visibility)
                .build();

        when(moderationPort.moderate(anyString()))
                .thenReturn(Mono.just(TextModerationResultFixture.withFlags().build()));

        // Then
        StepVerifier.create(service.createFrom(command))
                .verifyError(ModerationException.class);
    }
}
