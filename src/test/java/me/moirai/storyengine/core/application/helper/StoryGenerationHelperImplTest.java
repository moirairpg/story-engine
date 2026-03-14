package me.moirai.storyengine.core.application.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.moirai.storyengine.common.exception.ModerationException;
import me.moirai.storyengine.core.application.model.request.ChatMessage;
import me.moirai.storyengine.core.application.model.request.TextGenerationRequest;
import me.moirai.storyengine.core.application.model.result.TextGenerationResult;
import me.moirai.storyengine.core.application.model.result.TextGenerationResultFixture;
import me.moirai.storyengine.core.application.model.result.TextModerationResult;
import me.moirai.storyengine.core.application.model.result.TextModerationResultFixture;
import me.moirai.storyengine.core.application.usecase.discord.DiscordMessageData;
import me.moirai.storyengine.core.port.DiscordChannelPort;
import me.moirai.storyengine.core.port.StorySummarizationPort;
import me.moirai.storyengine.core.port.TextCompletionPort;
import me.moirai.storyengine.core.port.TextModerationPort;
import me.moirai.storyengine.infrastructure.outbound.adapter.request.ModelConfigurationRequest;
import me.moirai.storyengine.infrastructure.outbound.adapter.request.ModerationConfigurationRequestFixture;
import me.moirai.storyengine.infrastructure.outbound.adapter.request.StoryGenerationRequest;
import me.moirai.storyengine.infrastructure.outbound.adapter.request.StoryGenerationRequestFixture;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class StoryGenerationHelperImplTest {

    @Mock
    private LorebookEnrichmentHelper lorebookEnrichmentHelper;

    @Mock
    private StorySummarizationPort summarizationPort;

    @Mock
    private PersonaEnrichmentHelper personaEnrichmentPort;

    @Mock
    private DiscordChannelPort discordChannelOperationsPort;

    @Mock
    private TextCompletionPort textCompletionPort;

    @Mock
    private TextModerationPort textModerationPort;

    @Captor
    private ArgumentCaptor<TextGenerationRequest> textGenerationRequestCaptor;

    @InjectMocks
    private StoryGenerationHelperImpl adapter;

    @Test
    void givenValidMessage_whenExecute_thenShouldProcessAndSendResponse() {

        // Given
        String personaDescription = "This is a persona";
        String lorebook = "This is a lorebook";
        String summary = "This is a story summary";
        String channelId = "CHNLID";

        StoryGenerationRequest query = StoryGenerationRequestFixture.create()
                .channelId(channelId)
                .build();

        Map<String, Object> context = new HashMap<>();
        context.put("messageHistory", List.of("TestUser said: history",
                "AnotherUser said: another message",
                "TheOtherUser said: another message",
                "Cherokee said: another message",
                "YetAnotherUser said: yet another message"));

        context.put("lorebook", lorebook);
        context.put("persona", personaDescription);
        context.put("summary", summary);

        TextGenerationResult generationResult = TextGenerationResultFixture.create().build();
        TextModerationResult moderationResult = TextModerationResultFixture.withoutFlags().build();

        when(summarizationPort.summarizeContextWith(anyMap(), any(StoryGenerationRequest.class)))
                .thenReturn(Mono.just(context));

        when(lorebookEnrichmentHelper.enrichContextWithLorebook(anyList(), anyString(),
                any(ModelConfigurationRequest.class)))
                .thenReturn(context);

        when(personaEnrichmentPort.enrichContextWithPersona(anyMap(), anyString(),
                any(ModelConfigurationRequest.class)))
                .thenReturn(Mono.just(context));

        when(textCompletionPort.generateTextFrom(any(TextGenerationRequest.class)))
                .thenReturn(Mono.just(generationResult));

        when(textModerationPort.moderate(anyString()))
                .thenReturn(Mono.just(moderationResult));

        when(discordChannelOperationsPort.sendTextMessageTo(eq(channelId), anyString()))
                .thenReturn(mock(DiscordMessageData.class));

        // When
        Mono<Void> result = adapter.continueStory(query);

        // Then
        StepVerifier.create(result).verifyComplete();
        verify(textCompletionPort).generateTextFrom(textGenerationRequestCaptor.capture());
        verify(discordChannelOperationsPort).sendTextMessageTo(eq(channelId), anyString());

        List<ChatMessage> messagesSentToAi = textGenerationRequestCaptor.getValue().getMessages();
        assertThat(messagesSentToAi).isNotNull().isNotEmpty().hasSize(13);
    }

    @Test
    void givenValidMessage_whenExecute_andGameModeIsRpg_thenShouldProcessAndSendResponse() {

        // Given
        String personaDescription = "This is a persona";
        String lorebook = "This is a lorebook";
        String summary = "This is a story summary";
        String channelId = "CHNLID";

        StoryGenerationRequest query = StoryGenerationRequestFixture.create()
                .channelId(channelId)
                .gameMode("RPG")
                .build();

        Map<String, Object> context = new HashMap<>();
        context.put("messageHistory", List.of("TestUser said: history",
                "AnotherUser said: another message",
                "TheOtherUser said: another message",
                "Cherokee said: another message",
                "YetAnotherUser said: yet another message"));

        context.put("lorebook", lorebook);
        context.put("persona", personaDescription);
        context.put("summary", summary);

        TextGenerationResult generationResult = TextGenerationResultFixture.create().build();
        TextModerationResult moderationResult = TextModerationResultFixture.withoutFlags().build();

        when(summarizationPort.summarizeContextWith(anyMap(), any(StoryGenerationRequest.class)))
                .thenReturn(Mono.just(context));

        when(lorebookEnrichmentHelper.enrichContextWithLorebookForRpg(anyList(), anyString(),
                any(ModelConfigurationRequest.class)))
                .thenReturn(context);

        when(personaEnrichmentPort.enrichContextWithPersona(anyMap(), anyString(),
                any(ModelConfigurationRequest.class)))
                .thenReturn(Mono.just(context));

        when(textCompletionPort.generateTextFrom(any(TextGenerationRequest.class)))
                .thenReturn(Mono.just(generationResult));

        when(textModerationPort.moderate(anyString()))
                .thenReturn(Mono.just(moderationResult));

        when(discordChannelOperationsPort.sendTextMessageTo(eq(channelId), anyString()))
                .thenReturn(mock(DiscordMessageData.class));

        // When
        Mono<Void> result = adapter.continueStory(query);

        // Then
        StepVerifier.create(result).verifyComplete();
        verify(textCompletionPort).generateTextFrom(textGenerationRequestCaptor.capture());
        verify(discordChannelOperationsPort).sendTextMessageTo(eq(channelId), anyString());

        List<ChatMessage> messagesSentToAi = textGenerationRequestCaptor.getValue().getMessages();
        assertThat(messagesSentToAi).isNotNull().isNotEmpty().hasSize(13);
    }

    @Test
    void givenValidMessage_whenExecute_andModerationIsDisabled_thenShouldProcessAndSendResponse() {

        // Given
        String personaDescription = "This is a persona";
        String lorebook = "This is a lorebook";
        String summary = "This is a story summary";
        String channelId = "CHNLID";

        StoryGenerationRequest query = StoryGenerationRequestFixture.create()
                .channelId(channelId)
                .gameMode("RPG")
                .moderation(ModerationConfigurationRequestFixture.disabled())
                .build();

        Map<String, Object> context = new HashMap<>();
        context.put("messageHistory", List.of("TestUser said: history",
                "AnotherUser said: another message",
                "TheOtherUser said: another message",
                "Cherokee said: another message",
                "YetAnotherUser said: yet another message"));

        context.put("lorebook", lorebook);
        context.put("persona", personaDescription);
        context.put("summary", summary);

        TextGenerationResult generationResult = TextGenerationResultFixture.create().build();
        TextModerationResult badModerationResult = TextModerationResultFixture.withFlags().build();

        when(summarizationPort.summarizeContextWith(anyMap(), any(StoryGenerationRequest.class)))
                .thenReturn(Mono.just(context));

        when(lorebookEnrichmentHelper.enrichContextWithLorebookForRpg(anyList(), anyString(),
                any(ModelConfigurationRequest.class)))
                .thenReturn(context);

        when(personaEnrichmentPort.enrichContextWithPersona(anyMap(), anyString(),
                any(ModelConfigurationRequest.class)))
                .thenReturn(Mono.just(context));

        when(textCompletionPort.generateTextFrom(any(TextGenerationRequest.class)))
                .thenReturn(Mono.just(generationResult));

        when(textModerationPort.moderate(anyString()))
                .thenReturn(Mono.just(badModerationResult));

        when(discordChannelOperationsPort.sendTextMessageTo(eq(channelId), anyString()))
                .thenReturn(mock(DiscordMessageData.class));

        // When
        Mono<Void> result = adapter.continueStory(query);

        // Then
        StepVerifier.create(result).verifyComplete();
        verify(textCompletionPort).generateTextFrom(textGenerationRequestCaptor.capture());
        verify(discordChannelOperationsPort).sendTextMessageTo(eq(channelId), anyString());

        List<ChatMessage> messagesSentToAi = textGenerationRequestCaptor.getValue().getMessages();
        assertThat(messagesSentToAi).isNotNull().isNotEmpty().hasSize(13);
    }

    @Test
    void givenInappropriateContentFound_whenExecute_andModerationIsDisabled_thenShouldProcessAndSendResponse() {

        // Given
        String personaDescription = "This is a persona";
        String lorebook = "This is a lorebook";
        String summary = "This is a story summary";
        String channelId = "CHNLID";

        StoryGenerationRequest query = StoryGenerationRequestFixture.create()
                .channelId(channelId)
                .gameMode("RPG")
                .moderation(ModerationConfigurationRequestFixture.disabled())
                .build();

        Map<String, Object> context = new HashMap<>();
        context.put("messageHistory", List.of("TestUser said: history",
                "AnotherUser said: another message",
                "TheOtherUser said: another message",
                "Cherokee said: another message",
                "YetAnotherUser said: yet another message"));

        context.put("lorebook", lorebook);
        context.put("persona", personaDescription);
        context.put("summary", summary);

        TextGenerationResult generationResult = TextGenerationResultFixture.create().build();
        TextModerationResult badModerationResult = TextModerationResultFixture.withFlags().build();

        when(summarizationPort.summarizeContextWith(anyMap(), any(StoryGenerationRequest.class)))
                .thenReturn(Mono.just(context));

        when(lorebookEnrichmentHelper.enrichContextWithLorebookForRpg(anyList(), anyString(),
                any(ModelConfigurationRequest.class)))
                .thenReturn(context);

        when(textModerationPort.moderate(anyString()))
                .thenReturn(Mono.just(badModerationResult));

        when(personaEnrichmentPort.enrichContextWithPersona(anyMap(), anyString(),
                any(ModelConfigurationRequest.class)))
                .thenReturn(Mono.just(context));

        when(textCompletionPort.generateTextFrom(any(TextGenerationRequest.class)))
                .thenReturn(Mono.just(generationResult));

        when(discordChannelOperationsPort.sendTextMessageTo(eq(channelId), anyString()))
                .thenReturn(mock(DiscordMessageData.class));

        // When
        Mono<Void> result = adapter.continueStory(query);

        // Then
        StepVerifier.create(result).verifyComplete();
        verify(textCompletionPort).generateTextFrom(textGenerationRequestCaptor.capture());
        verify(discordChannelOperationsPort).sendTextMessageTo(eq(channelId), anyString());

        List<ChatMessage> messagesSentToAi = textGenerationRequestCaptor.getValue().getMessages();
        assertThat(messagesSentToAi).isNotNull().isNotEmpty().hasSize(13);
    }

    @Test
    void givenValidMessage_whenExecute_andNoEntriesFound_thenShouldProcessAndSendResponse() {

        // Given
        String personaDescription = "This is a persona";
        String summary = "This is a story summary";
        String channelId = "CHNLID";

        StoryGenerationRequest query = StoryGenerationRequestFixture.create()
                .channelId(channelId)
                .build();

        Map<String, Object> context = new HashMap<>();
        context.put("messageHistory", List.of("TestUser said: history",
                "AnotherUser said: another message",
                "TheOtherUser said: another message",
                "Cherokee said: another message",
                "YetAnotherUser said: yet another message"));

        context.put("persona", personaDescription);
        context.put("summary", summary);

        TextGenerationResult generationResult = TextGenerationResultFixture.create().build();
        TextModerationResult moderationResult = TextModerationResultFixture.withoutFlags().build();

        when(summarizationPort.summarizeContextWith(anyMap(), any(StoryGenerationRequest.class)))
                .thenReturn(Mono.just(context));

        when(lorebookEnrichmentHelper.enrichContextWithLorebook(anyList(), anyString(),
                any(ModelConfigurationRequest.class)))
                .thenReturn(context);

        when(personaEnrichmentPort.enrichContextWithPersona(anyMap(), anyString(),
                any(ModelConfigurationRequest.class)))
                .thenReturn(Mono.just(context));

        when(textCompletionPort.generateTextFrom(any(TextGenerationRequest.class)))
                .thenReturn(Mono.just(generationResult));

        when(textModerationPort.moderate(anyString()))
                .thenReturn(Mono.just(moderationResult));

        when(discordChannelOperationsPort.sendTextMessageTo(eq(channelId), anyString()))
                .thenReturn(mock(DiscordMessageData.class));

        // When
        Mono<Void> result = adapter.continueStory(query);

        // Then
        StepVerifier.create(result).verifyComplete();
        verify(textCompletionPort).generateTextFrom(textGenerationRequestCaptor.capture());
        verify(discordChannelOperationsPort).sendTextMessageTo(eq(channelId), anyString());

        List<ChatMessage> messagesSentToAi = textGenerationRequestCaptor.getValue().getMessages();
        assertThat(messagesSentToAi).isNotNull().isNotEmpty().hasSize(12);
    }

    @Test
    void givenValidMessage_whenExecute_andOutputIsBiggerThanDiscordLimit_thenShouldTrimOutputAndSendResponse() {

        // Given
        String personaDescription = "This is a persona";
        String lorebook = "This is a lorebook";
        String summary = "This is a story summary";
        String channelId = "CHNLID";

        StoryGenerationRequest query = StoryGenerationRequestFixture.create()
                .channelId(channelId)
                .build();

        Map<String, Object> context = new HashMap<>();
        context.put("messageHistory", List.of("TestUser said: history",
                "AnotherUser said: another message",
                "TheOtherUser said: another message",
                "Cherokee said: another message",
                "YetAnotherUser said: yet another message"));

        context.put("lorebook", lorebook);
        context.put("persona", personaDescription);
        context.put("summary", summary);

        TextGenerationResult generationResult = TextGenerationResultFixture.create()
                .outputText(
                        "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec pede justo, fringilla vel, aliquet nec, vulputate eget, arcu. In enim justo, rhoncus ut, imperdiet a, venenatis vitae, justo. Nullam dictum felis eu pede mollis pretium. Integer tincidunt. Cras dapibus. Vivamus elementum semper nisi. Aenean vulputate eleifend tellus. Aenean leo ligula, porttitor eu, consequat vitae, eleifend ac, enim. Aliquam lorem ante, dapibus in, viverra quis, feugiat a, tellus. Phasellus viverra nulla ut metus varius laoreet. Quisque rutrum. Aenean imperdiet. Etiam ultricies nisi vel augue. Curabitur ullamcorper ultricies nisi. Nam eget dui. Etiam rhoncus. Maecenas tempus, tellus eget condimentum rhoncus, sem quam semper libero, sit amet adipiscing sem neque sed ipsum. Nam quam nunc, blandit vel, luctus pulvinar, hendrerit id, lorem. Maecenas nec odio et ante tincidunt tempus. Donec vitae sapien ut libero venenatis faucibus. Nullam quis ante. Etiam sit amet orci eget eros faucibus tincidunt. Duis leo. Sed fringilla mauris sit amet nibh. Donec sodales sagittis magna. Sed consequat, leo eget bibendum sodales, augue velit cursus nunc, quis gravida magna mi a libero. Fusce vulputate eleifend sapien. Vestibulum purus quam, scelerisque ut, mollis sed, nonummy id, metus. Nullam accumsan lorem in dui. Cras ultricies mi eu turpis hendrerit fringilla. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; In ac dui quis mi consectetuer lacinia. Nam pretium turpis et arcu. Duis arcu tortor, suscipit eget, imperdiet nec, imperdiet iaculis, ipsum. Sed aliquam ultrices mauris. Integer ante arcu, accumsan a, consectetuer eget, posuere ut, mauris. Praesent adipiscing. Phasellus ullamcorper ipsum rutrum nunc. Nunc nonummy metus. Vestibulum volutpat pretium libero. Cras id dui. Aenean ut eros et nisl sagittis vestibulum. Nullam nulla eros, ultricies sit amet, nonummy id, imperdiet feugiat, pede. Sed lectus. Donec mollis hendrerit risus. Phasellus nec sem in justo pellentesque facilisis. Etiam imperdiet imperdiet orci. Nunc nec neque. Phasellus leo dolor, tempus non, auctor et, hendrerit quis, nisi. Curabitur ligula sapien, tincidunt non, euismod vitae, posuere imperdiet, leo. Maecenas malesuada. Praesent congue erat at massa.")
                .build();

        TextModerationResult moderationResult = TextModerationResultFixture.withoutFlags().build();

        when(summarizationPort.summarizeContextWith(anyMap(), any(StoryGenerationRequest.class)))
                .thenReturn(Mono.just(context));

        when(lorebookEnrichmentHelper.enrichContextWithLorebook(anyList(), anyString(),
                any(ModelConfigurationRequest.class)))
                .thenReturn(context);

        when(personaEnrichmentPort.enrichContextWithPersona(anyMap(), anyString(),
                any(ModelConfigurationRequest.class)))
                .thenReturn(Mono.just(context));

        when(textCompletionPort.generateTextFrom(any(TextGenerationRequest.class)))
                .thenReturn(Mono.just(generationResult));

        when(textModerationPort.moderate(anyString()))
                .thenReturn(Mono.just(moderationResult));

        when(discordChannelOperationsPort.sendTextMessageTo(eq(channelId), anyString()))
                .thenReturn(mock(DiscordMessageData.class));

        // When
        Mono<Void> result = adapter.continueStory(query);

        // Then
        StepVerifier.create(result).verifyComplete();
        verify(textCompletionPort).generateTextFrom(textGenerationRequestCaptor.capture());
        verify(discordChannelOperationsPort).sendTextMessageTo(eq(channelId), anyString());

        List<ChatMessage> messagesSentToAi = textGenerationRequestCaptor.getValue().getMessages();
        assertThat(messagesSentToAi).isNotNull().isNotEmpty().hasSize(13);
    }

    @Test
    void givenValidMessage_whenExecute_andContextModifiersEmpty_thenShouldProcessAndSendResponse() {

        // Given
        String personaDescription = "This is a persona";
        String lorebook = "This is a lorebook";
        String summary = "This is a story summary";
        String channelId = "CHNLID";

        StoryGenerationRequest query = StoryGenerationRequestFixture.create()
                .channelId(channelId)
                .authorsNote(null)
                .remember(null)
                .nudge(null)
                .bump(null)
                .build();

        Map<String, Object> context = new HashMap<>();
        context.put("messageHistory", List.of("TestUser said: history",
                "AnotherUser said: another message",
                "TheOtherUser said: another message",
                "Cherokee said: another message",
                "YetAnotherUser said: yet another message"));

        context.put("lorebook", lorebook);
        context.put("persona", personaDescription);
        context.put("summary", summary);

        TextGenerationResult generationResult = TextGenerationResultFixture.create().build();
        TextModerationResult moderationResult = TextModerationResultFixture.withoutFlags().build();

        when(summarizationPort.summarizeContextWith(anyMap(), any(StoryGenerationRequest.class)))
                .thenReturn(Mono.just(context));

        when(lorebookEnrichmentHelper.enrichContextWithLorebook(anyList(), anyString(),
                any(ModelConfigurationRequest.class)))
                .thenReturn(context);

        when(personaEnrichmentPort.enrichContextWithPersona(anyMap(), anyString(),
                any(ModelConfigurationRequest.class)))
                .thenReturn(Mono.just(context));

        when(textCompletionPort.generateTextFrom(any(TextGenerationRequest.class)))
                .thenReturn(Mono.just(generationResult));

        when(textModerationPort.moderate(anyString()))
                .thenReturn(Mono.just(moderationResult));

        when(discordChannelOperationsPort.sendTextMessageTo(eq(channelId), anyString()))
                .thenReturn(mock(DiscordMessageData.class));

        // When
        Mono<Void> result = adapter.continueStory(query);

        // Then
        StepVerifier.create(result).verifyComplete();
        verify(textCompletionPort).generateTextFrom(textGenerationRequestCaptor.capture());
        verify(discordChannelOperationsPort).sendTextMessageTo(eq(channelId), anyString());

        List<ChatMessage> messagesSentToAi = textGenerationRequestCaptor.getValue().getMessages();
        assertThat(messagesSentToAi).isNotNull().isNotEmpty().hasSize(8);
    }

    @Test
    void givenInappropriateInput_whenExecute_andModerationIsAbsolute_thenShouldThrowModerationException() {

        // Given
        String personaDescription = "This is a persona";
        String lorebook = "This is a lorebook";
        String summary = "This is a story summary";
        String channelId = "CHNLID";

        StoryGenerationRequest query = StoryGenerationRequestFixture.create()
                .channelId(channelId)
                .moderation(ModerationConfigurationRequestFixture.absoluteWithFlags())
                .build();

        Map<String, Object> context = new HashMap<>();
        context.put("messageHistory", List.of("TestUser said: history",
                "AnotherUser said: another message",
                "TheOtherUser said: another message",
                "Cherokee said: another message",
                "YetAnotherUser said: yet another message"));

        context.put("lorebook", lorebook);
        context.put("persona", personaDescription);
        context.put("summary", summary);

        TextModerationResult moderationResult = TextModerationResultFixture.withFlags().build();

        when(summarizationPort.summarizeContextWith(anyMap(), any(StoryGenerationRequest.class)))
                .thenReturn(Mono.just(context));

        when(lorebookEnrichmentHelper.enrichContextWithLorebook(anyList(), anyString(),
                any(ModelConfigurationRequest.class)))
                .thenReturn(context);

        when(personaEnrichmentPort.enrichContextWithPersona(anyMap(), anyString(),
                any(ModelConfigurationRequest.class)))
                .thenReturn(Mono.just(context));

        when(textModerationPort.moderate(anyString()))
                .thenReturn(Mono.just(moderationResult));

        // When
        Mono<Void> result = adapter.continueStory(query);

        // Then
        StepVerifier.create(result)
                .verifyErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(ModerationException.class);
                    assertThat(((ModerationException) error).getFlaggedTopics()).hasSize(2);
                });
    }

    @Test
    void givenInappropriateAiOutput_whenExecute_andModerationIsAbsolute_thenShouldThrowModerationException() {

        // Given
        String personaDescription = "This is a persona";
        String lorebook = "This is a lorebook";
        String summary = "This is a story summary";
        String channelId = "CHNLID";

        StoryGenerationRequest query = StoryGenerationRequestFixture.create()
                .channelId(channelId)
                .moderation(ModerationConfigurationRequestFixture.absoluteWithFlags())
                .build();

        Map<String, Object> context = new HashMap<>();
        context.put("messageHistory", List.of("TestUser said: history",
                "AnotherUser said: another message",
                "TheOtherUser said: another message",
                "Cherokee said: another message",
                "YetAnotherUser said: yet another message"));

        context.put("lorebook", lorebook);
        context.put("persona", personaDescription);
        context.put("summary", summary);

        TextGenerationResult generationResult = TextGenerationResultFixture.create().build();
        TextModerationResult goodModerationResult = TextModerationResultFixture.withoutFlags().build();
        TextModerationResult badModerationResult = TextModerationResultFixture.withFlags().build();

        when(summarizationPort.summarizeContextWith(anyMap(), any(StoryGenerationRequest.class)))
                .thenReturn(Mono.just(context));

        when(lorebookEnrichmentHelper.enrichContextWithLorebook(anyList(), anyString(),
                any(ModelConfigurationRequest.class)))
                .thenReturn(context);

        when(personaEnrichmentPort.enrichContextWithPersona(anyMap(), anyString(),
                any(ModelConfigurationRequest.class)))
                .thenReturn(Mono.just(context));

        when(textModerationPort.moderate(anyString()))
                .thenReturn(Mono.just(goodModerationResult))
                .thenReturn(Mono.just(badModerationResult));

        when(textCompletionPort.generateTextFrom(any(TextGenerationRequest.class)))
                .thenReturn(Mono.just(generationResult));

        // When
        Mono<Void> result = adapter.continueStory(query);

        // Then
        StepVerifier.create(result)
                .verifyErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(ModerationException.class);
                    assertThat(((ModerationException) error).getFlaggedTopics()).hasSize(2);
                });
    }

    @Test
    void givenInappropriateAiOutput_whenExecute_thenShouldThrowModerationException() {

        // Given
        String personaDescription = "This is a persona";
        String lorebook = "This is a lorebook";
        String summary = "This is a story summary";
        String channelId = "CHNLID";

        StoryGenerationRequest query = StoryGenerationRequestFixture.create()
                .channelId(channelId)
                .moderation(ModerationConfigurationRequestFixture.withFlags())
                .build();

        Map<String, Object> context = new HashMap<>();
        context.put("messageHistory", List.of("TestUser said: history",
                "AnotherUser said: another message",
                "TheOtherUser said: another message",
                "Cherokee said: another message",
                "YetAnotherUser said: yet another message"));

        context.put("lorebook", lorebook);
        context.put("persona", personaDescription);
        context.put("summary", summary);

        TextGenerationResult generationResult = TextGenerationResultFixture.create().build();
        TextModerationResult goodModerationResult = TextModerationResultFixture.withoutFlags().build();
        TextModerationResult badModerationResult = TextModerationResultFixture.withFlags().build();

        when(summarizationPort.summarizeContextWith(anyMap(), any(StoryGenerationRequest.class)))
                .thenReturn(Mono.just(context));

        when(lorebookEnrichmentHelper.enrichContextWithLorebook(anyList(), anyString(),
                any(ModelConfigurationRequest.class)))
                .thenReturn(context);

        when(personaEnrichmentPort.enrichContextWithPersona(anyMap(), anyString(),
                any(ModelConfigurationRequest.class)))
                .thenReturn(Mono.just(context));

        when(textModerationPort.moderate(anyString()))
                .thenReturn(Mono.just(goodModerationResult))
                .thenReturn(Mono.just(badModerationResult));

        when(textCompletionPort.generateTextFrom(any(TextGenerationRequest.class)))
                .thenReturn(Mono.just(generationResult));

        // When
        Mono<Void> result = adapter.continueStory(query);

        // Then
        StepVerifier.create(result)
                .verifyErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(ModerationException.class);
                    assertThat(((ModerationException) error).getFlaggedTopics()).hasSize(1);
                });
    }
}
