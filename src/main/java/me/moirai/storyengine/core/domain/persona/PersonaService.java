package me.moirai.storyengine.core.domain.persona;

import me.moirai.storyengine.core.port.inbound.persona.CreatePersona;
import reactor.core.publisher.Mono;

public interface PersonaService {

    Mono<Persona> createFrom(CreatePersona command);
}
