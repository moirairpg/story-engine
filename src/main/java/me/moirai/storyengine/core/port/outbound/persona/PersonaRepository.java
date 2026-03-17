package me.moirai.storyengine.core.port.outbound.persona;

import java.util.Optional;
import java.util.UUID;

import me.moirai.storyengine.core.domain.persona.Persona;
import me.moirai.storyengine.core.port.inbound.persona.SearchPersonas;
import me.moirai.storyengine.core.port.inbound.persona.SearchPersonasResult;

public interface PersonaRepository {

    Persona save(Persona persona);

    void deleteById(Long id);

    void deleteByPublicId(UUID publicId);

    Optional<Persona> findById(Long id);

    Optional<Persona> findByPublicId(UUID publicId);

    SearchPersonasResult search(SearchPersonas request);

    boolean existsById(Long id);

    boolean existsByPublicId(UUID publicId);
}
