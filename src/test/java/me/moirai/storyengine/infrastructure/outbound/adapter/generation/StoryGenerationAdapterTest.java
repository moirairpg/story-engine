package me.moirai.storyengine.infrastructure.outbound.adapter.generation;

import static org.assertj.core.api.Assertions.assertThat;
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
import me.moirai.storyengine.core.port.outbound.generation.TextModerationResult;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

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

        String botUsername = "TestBot";
        String botNickname = "Bot";
        String channelId = "channel-1";
        String adventureId = "adventure-1";
        String personaId = "persona-1";
        String generatedText = "Once upon a time.";

        DiscordMessageData message = DiscordMessageData.builder()
                .id("msg-1")
                .channelId(channelId)
                .content("User said: hello")
                .build();

        ModelConfigurationRequest modelConfig = ModelConfigurationRequest.builder()
                .aiModel(AiModelRequest.build("gpt4", "gpt-4", 8192))
                .maxTokenLimit(500)
                .temperature(0.8)
                .frequencyPenalty(0.0)
                .presencePenalty(0.0)
                .build();

        ModerationConfigurationRequest moderation = ModerationConfigurationRequest.build(true, true, null);

        StoryGenerationRequest request = StoryGenerationRequest.builder()
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

        Map<String, Object> lorebookContext = buildEnrichedContext(botUsername);
        Map<String, Object> summarizedContext = buildEnrichedContext(botUsername);
        Map<String, Object> personaContext = buildEnrichedContext(botUsername);

        TextGenerationResult generationResult = TextGenerationResult.builder()
                .outputText(generatedText)
                .build();

        TextModerationResult cleanModeration = TextModerationResult.builder()
                .contentFlagged(false)
                .flaggedTopics(List.of())
                .build();

        when(lorebookEnrichmentPort.enrichContextWithLorebook(any(), eq(adventureId), eq(modelConfig)))
                .thenReturn(lorebookContext);
        when(summarizationPort.summarizeContextWith(eq(lorebookContext), eq(request)))
                .thenReturn(Mono.just(summarizedContext));
        when(personaEnrichmentPort.enrichContextWithPersona(eq(summarizedContext), eq(personaId), eq(modelConfig)))
                .thenReturn(Mono.just(personaContext));
        when(textModerationPort.moderate(anyString()))
                .thenReturn(Mono.just(cleanModeration));
        when(textCompletionPort.generateTextFrom(any(TextGenerationRequest.class)))
                .thenReturn(Mono.just(generationResult));

        Mono<Void> result = adapter.continueStory(request);

        StepVerifier.create(result)
                .verifyComplete();

        verify(discordChannelPort).sendTextMessageTo(eq(channelId), anyString());
    }

    @Test
    public void shouldUseRpgLorebookEnrichmentWhenGameModeIsRpg() {

        String botUsername = "TestBot";
        String botNickname = "Bot";
        String channelId = "channel-1";
        String adventureId = "adventure-rpg";
        String personaId = "persona-1";
        String generatedText = "You enter the dungeon.";

        ModelConfigurationRequest modelConfig = ModelConfigurationRequest.builder()
                .aiModel(AiModelRequest.build("gpt4", "gpt-4", 8192))
                .maxTokenLimit(500)
                .temperature(0.8)
                .frequencyPenalty(0.0)
                .presencePenalty(0.0)
                .build();

        ModerationConfigurationRequest moderation = ModerationConfigurationRequest.build(true, true, null);

        StoryGenerationRequest request = StoryGenerationRequest.builder()
                .botUsername(botUsername)
                .botNickname(botNickname)
                .channelId(channelId)
                .adventureId(adventureId)
                .personaId(personaId)
                .gameMode("RPG")
                .modelConfiguration(modelConfig)
                .moderation(moderation)
                .build();

        Map<String, Object> lorebookContext = buildEnrichedContext(botUsername);
        Map<String, Object> summarizedContext = buildEnrichedContext(botUsername);
        Map<String, Object> personaContext = buildEnrichedContext(botUsername);

        TextGenerationResult generationResult = TextGenerationResult.builder()
                .outputText(generatedText)
                .build();

        TextModerationResult cleanModeration = TextModerationResult.builder()
                .contentFlagged(false)
                .flaggedTopics(List.of())
                .build();

        when(lorebookEnrichmentPort.enrichContextWithLorebookForRpg(any(), eq(adventureId), eq(modelConfig)))
                .thenReturn(lorebookContext);
        when(summarizationPort.summarizeContextWith(eq(lorebookContext), eq(request)))
                .thenReturn(Mono.just(summarizedContext));
        when(personaEnrichmentPort.enrichContextWithPersona(eq(summarizedContext), eq(personaId), eq(modelConfig)))
                .thenReturn(Mono.just(personaContext));
        when(textModerationPort.moderate(anyString()))
                .thenReturn(Mono.just(cleanModeration));
        when(textCompletionPort.generateTextFrom(any(TextGenerationRequest.class)))
                .thenReturn(Mono.just(generationResult));

        Mono<Void> result = adapter.continueStory(request);

        StepVerifier.create(result)
                .verifyComplete();

        verify(lorebookEnrichmentPort).enrichContextWithLorebookForRpg(any(), eq(adventureId), eq(modelConfig));
    }

    @Test
    public void shouldThrowModerationExceptionWhenInputContentIsFlagged() {

        String botUsername = "TestBot";
        String botNickname = "Bot";
        String channelId = "channel-1";
        String adventureId = "adventure-1";
        String personaId = "persona-1";

        ModelConfigurationRequest modelConfig = ModelConfigurationRequest.builder()
                .aiModel(AiModelRequest.build("gpt4", "gpt-4", 8192))
                .maxTokenLimit(500)
                .temperature(0.8)
                .frequencyPenalty(0.0)
                .presencePenalty(0.0)
                .build();

        ModerationConfigurationRequest moderation = ModerationConfigurationRequest.build(true, true, null);

        StoryGenerationRequest request = StoryGenerationRequest.builder()
                .botUsername(botUsername)
                .botNickname(botNickname)
                .channelId(channelId)
                .adventureId(adventureId)
                .personaId(personaId)
                .gameMode("Chat")
                .modelConfiguration(modelConfig)
                .moderation(moderation)
                .build();

        Map<String, Object> lorebookContext = buildEnrichedContext(botUsername);
        Map<String, Object> summarizedContext = buildEnrichedContext(botUsername);
        Map<String, Object> personaContext = buildEnrichedContext(botUsername);

        TextModerationResult flaggedModeration = TextModerationResult.builder()
                .contentFlagged(true)
                .flaggedTopics(List.of("violence"))
                .build();

        when(lorebookEnrichmentPort.enrichContextWithLorebook(any(), eq(adventureId), eq(modelConfig)))
                .thenReturn(lorebookContext);
        when(summarizationPort.summarizeContextWith(eq(lorebookContext), eq(request)))
                .thenReturn(Mono.just(summarizedContext));
        when(personaEnrichmentPort.enrichContextWithPersona(eq(summarizedContext), eq(personaId), eq(modelConfig)))
                .thenReturn(Mono.just(personaContext));
        when(textModerationPort.moderate(anyString()))
                .thenReturn(Mono.just(flaggedModeration));

        Mono<Void> result = adapter.continueStory(request);

        StepVerifier.create(result)
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(ModerationException.class);
                    ModerationException ex = (ModerationException) error;
                    assertThat(ex.getFlaggedTopics()).contains("violence");
                })
                .verify();
    }

    private Map<String, Object> buildEnrichedContext(String botUsername) {

        Map<String, Object> context = new HashMap<>();
        context.put("persona", "You are a storyteller.");
        context.put("personaName", botUsername);
        context.put("summary", botUsername + " said: The story begins.");
        context.put("lorebook", "");
        context.put("messageHistory", List.of(botUsername + " said: Hello."));
        return context;
    }
}
