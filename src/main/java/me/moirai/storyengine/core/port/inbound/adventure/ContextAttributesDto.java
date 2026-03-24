package me.moirai.storyengine.core.port.inbound.adventure;

public record ContextAttributesDto(
        String nudge,
        String authorsNote,
        String remember,
        String bump,
        Integer bumpFrequency) {
}
