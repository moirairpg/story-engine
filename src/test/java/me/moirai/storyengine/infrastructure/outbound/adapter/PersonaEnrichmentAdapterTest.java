package me.moirai.storyengine.infrastructure.outbound.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.port.outbound.generation.ChatMessagePort;
import me.moirai.storyengine.core.port.outbound.generation.TokenizerPort;
import me.moirai.storyengine.core.port.outbound.persona.PersonaRepository;
import me.moirai.storyengine.infrastructure.outbound.adapter.generation.PersonaEnrichmentAdapter;
import me.moirai.storyengine.infrastructure.outbound.adapter.request.ModelConfigurationRequestFixture;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
public class PersonaEnrichmentAdapterTest {

    @Mock
    private TokenizerPort tokenizerPort;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private ChatMessagePort chatMessageService;

    @InjectMocks
    private PersonaEnrichmentAdapter service;

    @Test
    public void enrichWithPersona_whenSufficientTokens_addPersonaAndMessages() {

        // Given
        var persona = PersonaFixture.privatePersona().build();
        ReflectionTestUtils.setField(persona, "publicId", PersonaFixture.PUBLIC_ID);
        var modelConfiguration = ModelConfigurationRequestFixture.gpt4Mini().build();
        var context = contextWithSummaryAndMessages(10);

        var expectedPersona = String.format(
                "[ DEBUG MODE ON: You are an actor interpreting the role of %s. %s's persona is as follows, and you are to maintain character during this conversation: %s ]",
                persona.getName(), persona.getName(), persona.getPersonality());

        when(personaRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(persona));
        when(tokenizerPort.getTokenCountFrom(anyString())).thenReturn(100);
        when(chatMessageService.addMessagesToContext(anyMap(), anyInt())).thenReturn(context);

        // When
        var processedContext = service.enrichContextWithPersona(context, persona.getPublicId(), modelConfiguration);

        // Then
        var personaResult = (String) processedContext.get("persona");
        var messageHistory = (List<String>) processedContext.get("messageHistory");

        assertThat(messageHistory).hasSize(10);
        assertThat(personaResult).isEqualTo(expectedPersona);
    }

    @Test
    public void enrichWithPersona_whenInsufficientTokensForMessages_thenOnlyPersonaAdded() {

        // Given
        var persona = PersonaFixture.privatePersona().build();
        ReflectionTestUtils.setField(persona, "publicId", PersonaFixture.PUBLIC_ID);
        var modelConfiguration = ModelConfigurationRequestFixture.gpt4Mini().build();
        var context = contextWithSummaryAndMessages(5);

        var expectedPersona = String.format(
                "[ DEBUG MODE ON: You are an actor interpreting the role of %s. %s's persona is as follows, and you are to maintain character during this conversation: %s ]",
                persona.getName(), persona.getName(), persona.getPersonality());

        when(personaRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(persona));
        when(tokenizerPort.getTokenCountFrom(anyString()))
                .thenReturn(100)
                .thenReturn(100)
                .thenReturn(100000);
        when(chatMessageService.addMessagesToContext(anyMap(), anyInt())).thenReturn(context);

        // When
        var processedContext = service.enrichContextWithPersona(context, persona.getPublicId(), modelConfiguration);

        // Then
        var personaResult = (String) processedContext.get("persona");
        var messageHistory = (List<String>) processedContext.get("messageHistory");

        assertThat(messageHistory).hasSize(5);
        assertThat(personaResult).isEqualTo(expectedPersona);
    }

    @Test
    public void enrichWithPersona_whenInsufficientTokensForPersona_thenExceptionIsThrown() {

        // Given
        var persona = PersonaFixture.privatePersona().build();
        ReflectionTestUtils.setField(persona, "publicId", PersonaFixture.PUBLIC_ID);
        var modelConfiguration = ModelConfigurationRequestFixture.gpt4Mini().build();
        var context = contextWithSummaryAndMessages(5);

        when(personaRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.of(persona));
        when(tokenizerPort.getTokenCountFrom(anyString()))
                .thenReturn(100000)
                .thenReturn(100);

        // Then
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> service.enrichContextWithPersona(context, persona.getPublicId(), modelConfiguration));
    }

    private Map<String, Object> contextWithSummaryAndMessages(int items) {

        var textMessages = new ArrayList<String>();
        for (int i = 0; i < items; i++) {
            textMessages.add(String.format("User said before test says: Message %s", i + 1));
        }

        var context = new HashMap<String, Object>();
        context.put("summary", "This is the summary");
        context.put("messageHistory", textMessages);
        context.put("lorebook", "This is the lorebook");

        return context;
    }
}
