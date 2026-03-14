package me.moirai.storyengine.core.application.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.core.domain.persona.Persona;
import me.moirai.storyengine.core.domain.persona.PersonaRepository;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.domain.port.TokenizerPort;
import me.moirai.storyengine.infrastructure.outbound.adapter.request.ModelConfigurationRequest;
import me.moirai.storyengine.infrastructure.outbound.adapter.request.ModelConfigurationRequestFixture;
import reactor.test.StepVerifier;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
public class PersonaEnrichmentHelperImplTest {

    @Mock
    private TokenizerPort tokenizerPort;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private ChatMessageHelper chatMessageService;

    @InjectMocks
    private PersonaEnrichmentHelperImpl service;

    @Test
    public void enrichWithPersona_whenSufficientTokens_addPersonaAndMessages() {

        // Given
        Persona persona = PersonaFixture.privatePersona().build();
        ModelConfigurationRequest modelConfiguration = ModelConfigurationRequestFixture.gpt4Mini().build();
        Map<String, Object> context = contextWithSummaryAndMessages(10);

        String expectedPersona = String.format(
                "[ DEBUG MODE ON: You are an actor interpreting the role of %s. %s's persona is as follows, and you are to maintain character during this conversation: %s ]",
                persona.getName(), persona.getName(), persona.getPersonality());

        when(personaRepository.findById(anyString())).thenReturn(Optional.of(persona));
        when(tokenizerPort.getTokenCountFrom(anyString())).thenReturn(100);

        when(chatMessageService.addMessagesToContext(anyMap(), anyInt()))
                .thenReturn(context);

        // Then
        StepVerifier.create(service.enrichContextWithPersona(context, persona.getId(), modelConfiguration))
                .assertNext(processedContext -> {
                    String personaResult = (String) processedContext.get("persona");
                    List<String> messageHistory = (List<String>) processedContext.get("messageHistory");

                    assertThat(messageHistory).hasSize(10);
                    assertThat(personaResult).isEqualTo(expectedPersona);
                })
                .verifyComplete();
    }

    @Test
    public void enrichWithPersona_whenInsufficientTokensForMessages_thenOnlyPersonaAdded() {

        // Given
        Persona persona = PersonaFixture.privatePersona().build();
        ModelConfigurationRequest modelConfiguration = ModelConfigurationRequestFixture.gpt4Mini().build();
        Map<String, Object> context = contextWithSummaryAndMessages(5);

        String expectedPersona = String.format(
                "[ DEBUG MODE ON: You are an actor interpreting the role of %s. %s's persona is as follows, and you are to maintain character during this conversation: %s ]",
                persona.getName(), persona.getName(), persona.getPersonality());

        when(personaRepository.findById(anyString())).thenReturn(Optional.of(persona));
        when(tokenizerPort.getTokenCountFrom(anyString()))
                .thenReturn(100)
                .thenReturn(100)
                .thenReturn(100000);

        when(chatMessageService.addMessagesToContext(anyMap(), anyInt()))
                .thenReturn(context);

        // Then
        StepVerifier.create(service.enrichContextWithPersona(context, persona.getId(), modelConfiguration))
                .assertNext(processedContext -> {
                    String personaResult = (String) processedContext.get("persona");
                    List<String> messageHistory = (List<String>) processedContext.get("messageHistory");

                    assertThat(messageHistory).hasSize(5);
                    assertThat(personaResult).isEqualTo(expectedPersona);
                })
                .verifyComplete();
    }

    @Test
    public void enrichWithPersona_whenInsufficientTokensForPersona_thenExceptionIsThrown() {

        // Given
        Persona persona = PersonaFixture.privatePersona().build();
        ModelConfigurationRequest modelConfiguration = ModelConfigurationRequestFixture.gpt4Mini().build();
        Map<String, Object> context = contextWithSummaryAndMessages(5);

        when(personaRepository.findById(anyString())).thenReturn(Optional.of(persona));
        when(tokenizerPort.getTokenCountFrom(anyString()))
                .thenReturn(100000)
                .thenReturn(100);

        // Then
        StepVerifier.create(service.enrichContextWithPersona(context, persona.getId(), modelConfiguration))
                .verifyError(IllegalStateException.class);
    }

    private Map<String, Object> contextWithSummaryAndMessages(int items) {

        List<String> textMessages = new ArrayList<>();
        for (int i = 0; i < items; i++) {
            textMessages.add(String.format("User said before test says: Message %s", i + 1));
        }

        Map<String, Object> context = new HashMap<>();
        context.put("summary", "This is the summary");
        context.put("messageHistory", textMessages);
        context.put("lorebook", "This is the lorebook");

        return context;
    }
}
