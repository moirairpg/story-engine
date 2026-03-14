package me.moirai.storyengine.infrastructure.inbound.rest.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.moirai.storyengine.common.usecases.UseCaseRunner;
import me.moirai.storyengine.common.web.SecurityContextAware;
import me.moirai.storyengine.core.port.inbound.completion.CompleteText;
import me.moirai.storyengine.core.port.inbound.completion.CompleteTextResult;
import me.moirai.storyengine.infrastructure.inbound.rest.request.TextCompletionRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.response.TextCompletionResponse;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/text-completion")
@Tag(name = "Text completion", description = "Endpoints for text generation in MoirAI")
public class CompletionController extends SecurityContextAware {

    private final UseCaseRunner useCaseRunner;

    public CompletionController(UseCaseRunner useCaseRunner) {
        this.useCaseRunner = useCaseRunner;
    }

    @PostMapping
    @ResponseStatus(code = HttpStatus.OK)
    @PreAuthorize("canRead(#request.personaId, 'Persona') && canRead(#request.worldId, 'World')")
    public Mono<TextCompletionResponse> generateText(@Valid @RequestBody TextCompletionRequest request) {

        return flatMapWithAuthenticatedUser(authenticatedUser -> {

            CompleteText command = CompleteText.builder()
                    .messages(request.getMessages().stream()
                            .map(this::mapMessages)
                            .toList())
                    .temperature(request.getTemperature())
                    .aiModel(request.getAiModel())
                    .personaId(request.getPersonaId())
                    .worldId(request.getWorldId())
                    .maxTokenLimit(request.getMaxTokenLimit())
                    .frequencyPenalty(request.getFrequencyPenalty())
                    .presencePenalty(request.getPresencePenalty())
                    .logitBias(request.getLogitBias())
                    .stopSequences(request.getStopSequences())
                    .moderationLevel(request.getModerationLevel())
                    .requesterId(authenticatedUser.getDiscordId())
                    .build();

            return useCaseRunner.run(command)
                    .map(this::toResponse);
        });
    }

    private CompleteText.Message mapMessages(TextCompletionRequest.Message message) {

        return CompleteText.Message.builder()
                .isAuthorBot(message.getIsAuthorBot())
                .messageContent(message.getMessageContent())
                .build();
    }

    private TextCompletionResponse toResponse(CompleteTextResult result) {

        return TextCompletionResponse.builder()
                .outputText(result.getOutputText())
                .completionTokens(result.getCompletionTokens())
                .promptTokens(result.getPromptTokens())
                .totalTokens(result.getTotalTokens())
                .completionTokens(result.getCompletionTokens())
                .tokenIds(result.getTokenIds())
                .tokens(result.getTokens())
                .build();
    }
}
