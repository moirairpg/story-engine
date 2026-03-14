package me.moirai.storyengine.core.application.port;

import me.moirai.storyengine.core.application.model.result.TextModerationResult;
import reactor.core.publisher.Mono;

public interface TextModerationPort {

    Mono<TextModerationResult> moderate(String text);
}
