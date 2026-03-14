package me.moirai.storyengine.core.application.port;

import java.util.Map;

import me.moirai.storyengine.core.port.outbound.ModelConfigurationRequest;
import reactor.core.publisher.Mono;

public interface PersonaEnrichmentPort {

    Mono<Map<String, Object>> enrichContextWithPersona(Map<String, Object> processedContext, String personaId,
            ModelConfigurationRequest modelConfiguration);
}
