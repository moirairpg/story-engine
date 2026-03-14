package me.moirai.storyengine.core.port;

import me.moirai.storyengine.core.port.outbound.TextModerationResult;
import reactor.core.publisher.Mono;

public interface TextModerationPort {

    Mono<TextModerationResult> moderate(String text);
}
