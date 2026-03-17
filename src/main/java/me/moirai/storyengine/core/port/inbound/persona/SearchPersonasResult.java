package me.moirai.storyengine.core.port.inbound.persona;

import java.util.List;

public record SearchPersonasResult(
        int page,
        int items,
        long totalItems,
        int totalPages,
        List<PersonaDetails> results) {
}