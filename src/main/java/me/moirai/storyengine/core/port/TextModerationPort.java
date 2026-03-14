package me.moirai.storyengine.core.port;

import me.moirai.storyengine.core.application.model.result.TextModerationResult;
import reactor.core.publisher.Mono;

public interface TextModerationPort {

    Mono<TextModerationResult> moderate(String text);
}
