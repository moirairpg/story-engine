package me.moirai.storyengine.core.port.inbound.adventure;

public record ContextAttributesDto(
        String nudge,
        String authorsNote,
        String scene,
        String bump,
        Integer bumpFrequency) {
}
