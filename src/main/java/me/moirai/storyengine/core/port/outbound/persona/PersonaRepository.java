package me.moirai.storyengine.core.port.outbound.persona;

import java.util.Optional;
import java.util.UUID;

import me.moirai.storyengine.core.domain.persona.Persona;

public interface PersonaRepository {

    Persona save(Persona persona);

    void deleteByPublicId(UUID publicId);

    Optional<Persona> findById(Long id);

    Optional<Persona> findByPublicId(UUID publicId);
}
