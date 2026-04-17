package me.moirai.storyengine.infrastructure.inbound.rest.request;

import me.moirai.storyengine.infrastructure.inbound.rest.validation.Moderated;

public record ContextAttributesRequest(
        @Moderated String authorsNote,
        @Moderated String scene,
        @Moderated String nudge,
        @Moderated String bump,
        Integer bumpFrequency) {
}
