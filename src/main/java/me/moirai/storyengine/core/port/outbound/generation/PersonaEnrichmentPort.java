package me.moirai.storyengine.core.port.outbound.generation;

import java.util.Map;

import reactor.core.publisher.Mono;

public interface PersonaEnrichmentPort {

    Mono<Map<String, Object>> enrichContextWithPersona(Map<String, Object> processedContext, String personaId,
            ModelConfigurationRequest modelConfiguration);
}
