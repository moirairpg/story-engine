package me.moirai.storyengine.core.domain.persona;

import java.util.Optional;

import me.moirai.storyengine.core.port.inbound.persona.SearchPersonas;
import me.moirai.storyengine.core.port.inbound.persona.SearchPersonasResult;

public interface PersonaRepository {

    Persona save(Persona persona);

    void deleteById(String id);

    Optional<Persona> findById(String id);

    SearchPersonasResult search(SearchPersonas request);

    boolean existsById(String id);
}