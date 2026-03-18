package me.moirai.storyengine.core.port.outbound.generation;

import java.util.Map;
import java.util.UUID;

public interface PersonaEnrichmentPort {

    Map<String, Object> enrichContextWithPersona(
            Map<String, Object> processedContext,
            UUID personaId,
            ModelConfigurationRequest modelConfiguration);
}
