package me.moirai.storyengine.core.port.outbound.generation;

import reactor.core.publisher.Mono;

public interface ReactiveTextModerationPort {

    Mono<TextModerationResult> moderate(String text);
}
