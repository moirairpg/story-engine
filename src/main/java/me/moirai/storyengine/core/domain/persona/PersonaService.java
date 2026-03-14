package me.moirai.storyengine.core.domain.persona;

import me.moirai.storyengine.core.application.usecase.persona.request.CreatePersona;
import reactor.core.publisher.Mono;

public interface PersonaService {

    Mono<Persona> createFrom(CreatePersona command);
}
