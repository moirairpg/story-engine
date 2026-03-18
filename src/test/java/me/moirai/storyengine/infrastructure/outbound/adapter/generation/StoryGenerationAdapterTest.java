package me.moirai.storyengine.infrastructure.outbound.adapter.generation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.exception.ModerationException;
import me.moirai.storyengine.core.domain.adventure.AdventureFixture;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.port.inbound.discord.DiscordMessageData;
import me.moirai.storyengine.core.port.outbound.discord.DiscordChannelPort;
import me.moirai.storyengine.core.port.outbound.generation.AiModelRequest;
import me.moirai.storyengine.core.port.outbound.generation.LorebookEnrichmentPort;
import me.moirai.storyengine.core.port.outbound.generation.ModerationConfigurationRequest;
import me.moirai.storyengine.core.port.outbound.generation.ModelConfigurationRequest;
import me.moirai.storyengine.core.port.outbound.generation.PersonaEnrichmentPort;
import me.moirai.storyengine.core.port.outbound.generation.StorySummarizationPort;
import me.moirai.storyengine.core.port.outbound.generation.StoryGenerationRequest;
import me.moirai.storyengine.core.port.outbound.generation.TextCompletionPort;
import me.moirai.storyengine.core.port.outbound.generation.TextGenerationRequest;
import me.moirai.storyengine.core.port.outbound.generation.TextGenerationResult;
import me.moirai.storyengine.core.port.outbound.generation.TextModerationPort;
import me.moirai.storyengine.core.port.outbound.generation.TextModerationResultFixture;

@ExtendWith(MockitoExtension.class)
public class StoryGenerationAdapterTest {

    @Mock
    private StorySummarizationPort summarizationPort;

    @Mock
    private DiscordChannelPort discordChannelPort;

    @Mock
    private LorebookEnrichmentPort lorebookEnrichmentPort;

    @Mock
    private PersonaEnrichmentPort personaEnrichmentPort;

    @Mock
    private TextCompletionPort textCompletionPort;

    @Mock
    private TextModerationPort textModerationPort;

    @InjectMocks
    private StoryGenerationAdapter adapter;

    @Test
    public void shouldExecuteFullPipelineWhenContinueStoryIsCalled() {

        var botUsername = "TestBot";
        var botNickname = "Bot";
        var channelId = "channel-1";
        var adventureId = AdventureFixture.PUBLIC_ID;
        var personaId = PersonaFixture.PUBLIC_ID;
        var generatedText = "Once upon a time.";

        var message = DiscordMessageData.builder()
                .id("msg-1")
                .channelId(channelId)
                .content("User said: hello")
                .build();

        var modelConfig = ModelConfigurationRequest.builder()
                .aiModel(AiModelRequest.build("gpt4", "gpt-4", 8192))
                .maxTokenLimit(500)
                .temperature(0.8)
                .frequencyPenalty(0.0)
                .presencePenalty(0.0)
                .build();

        var moderation = ModerationConfigurationRequest.build(true, true, null);

        var request = StoryGenerationRequest.builder()
                .botUsername(botUsername)
                .botNickname(botNickname)
                .channelId(channelId)
                .adventureId(adventureId)
                .personaId(personaId)
                .gameMode("Chat")
                .modelConfiguration(modelConfig)
                .moderation(moderation)
                .messageHistory(List.of(message))
                .build();

        var lorebookContext = buildEnrichedContext(botUsername);
        var summarizedContext = buildEnrichedContext(botUsername);
        var personaContext = buildEnrichedContext(botUsername);

        var generationResult = TextGenerationResult.builder()
                .outputText(generatedText)
                .build();

        var cleanModeration = TextModerationResultFixture.withoutFlags();

        when(lorebookEnrichmentPort.enrichContextWithLorebook(any(), eq(adventureId), eq(modelConfig)))
                .thenReturn(lorebookContext);
        when(summarizationPort.summarizeContextWith(eq(lorebookContext), eq(request)))
                .thenReturn(summarizedContext);
        when(personaEnrichmentPort.enrichContextWithPersona(eq(summarizedContext), eq(personaId), eq(modelConfig)))
                .thenReturn(personaContext);
        when(textModerationPort.moderate(anyString()))
                .thenReturn(cleanModeration);
        when(textCompletionPort.generateTextFrom(any(TextGenerationRequest.class)))
                .thenReturn(generationResult);

        // When
        adapter.continueStory(request);

        // Then
        verify(discordChannelPort).sendTextMessageTo(eq(channelId), anyString());
    }

    @Test
    public void shouldUseRpgLorebookEnrichmentWhenGameModeIsRpg() {

        var botUsername = "TestBot";
        var botNickname = "Bot";
        var channelId = "channel-1";
        var adventureId = AdventureFixture.PUBLIC_ID;
        var personaId = PersonaFixture.PUBLIC_ID;
        var generatedText = "You enter the dungeon.";

        var modelConfig = ModelConfigurationRequest.builder()
                .aiModel(AiModelRequest.build("gpt4", "gpt-4", 8192))
                .maxTokenLimit(500)
                .temperature(0.8)
                .frequencyPenalty(0.0)
                .presencePenalty(0.0)
                .build();

        var moderation = ModerationConfigurationRequest.build(true, true, null);

        var request = StoryGenerationRequest.builder()
                .botUsername(botUsername)
                .botNickname(botNickname)
                .channelId(channelId)
                .adventureId(adventureId)
                .personaId(personaId)
                .gameMode("RPG")
                .modelConfiguration(modelConfig)
                .moderation(moderation)
                .build();

        var lorebookContext = buildEnrichedContext(botUsername);
        var summarizedContext = buildEnrichedContext(botUsername);
        var personaContext = buildEnrichedContext(botUsername);

        var generationResult = TextGenerationResult.builder()
                .outputText(generatedText)
                .build();

        var cleanModeration = TextModerationResultFixture.withoutFlags();

        when(lorebookEnrichmentPort.enrichContextWithLorebookForRpg(any(), eq(adventureId), eq(modelConfig)))
                .thenReturn(lorebookContext);
        when(summarizationPort.summarizeContextWith(eq(lorebookContext), eq(request)))
                .thenReturn(summarizedContext);
        when(personaEnrichmentPort.enrichContextWithPersona(eq(summarizedContext), eq(personaId), eq(modelConfig)))
                .thenReturn(personaContext);
        when(textModerationPort.moderate(anyString()))
                .thenReturn(cleanModeration);
        when(textCompletionPort.generateTextFrom(any(TextGenerationRequest.class)))
                .thenReturn(generationResult);

        // When
        adapter.continueStory(request);

        // Then
        verify(lorebookEnrichmentPort).enrichContextWithLorebookForRpg(any(), eq(adventureId), eq(modelConfig));
    }

    @Test
    public void shouldThrowModerationExceptionWhenInputContentIsFlagged() {

        var botUsername = "TestBot";
        var botNickname = "Bot";
        var channelId = "channel-1";
        var adventureId = AdventureFixture.PUBLIC_ID;
        var personaId = PersonaFixture.PUBLIC_ID;

        var modelConfig = ModelConfigurationRequest.builder()
                .aiModel(AiModelRequest.build("gpt4", "gpt-4", 8192))
                .maxTokenLimit(500)
                .temperature(0.8)
                .frequencyPenalty(0.0)
                .presencePenalty(0.0)
                .build();

        var moderation = ModerationConfigurationRequest.build(true, true, null);

        var request = StoryGenerationRequest.builder()
                .botUsername(botUsername)
                .botNickname(botNickname)
                .channelId(channelId)
                .adventureId(adventureId)
                .personaId(personaId)
                .gameMode("Chat")
                .modelConfiguration(modelConfig)
                .moderation(moderation)
                .build();

        var lorebookContext = buildEnrichedContext(botUsername);
        var summarizedContext = buildEnrichedContext(botUsername);
        var personaContext = buildEnrichedContext(botUsername);

        var flaggedModeration = TextModerationResultFixture.withFlags();

        when(lorebookEnrichmentPort.enrichContextWithLorebook(any(), eq(adventureId), eq(modelConfig)))
                .thenReturn(lorebookContext);
        when(summarizationPort.summarizeContextWith(eq(lorebookContext), eq(request)))
                .thenReturn(summarizedContext);
        when(personaEnrichmentPort.enrichContextWithPersona(eq(summarizedContext), eq(personaId), eq(modelConfig)))
                .thenReturn(personaContext);
        when(textModerationPort.moderate(anyString()))
                .thenReturn(flaggedModeration);

        // Then
        assertThatExceptionOfType(ModerationException.class)
                .isThrownBy(() -> adapter.continueStory(request))
                .satisfies(ex -> assertThat(ex.getFlaggedTopics()).contains("violence"));
    }

    private Map<String, Object> buildEnrichedContext(String botUsername) {

        var context = new HashMap<String, Object>();
        context.put("persona", "You are a storyteller.");
        context.put("personaName", botUsername);
        context.put("summary", botUsername + " said: The story begins.");
        context.put("lorebook", "");
        context.put("messageHistory", List.of(botUsername + " said: Hello."));
        return context;
    }
}
