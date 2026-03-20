package me.moirai.storyengine.core.port.outbound.persona;

import java.util.Optional;
import java.util.UUID;

import me.moirai.storyengine.core.port.inbound.persona.PersonaDetails;

public interface PersonaReader {

    Optional<PersonaDetails> getPersonaById(UUID publicId);
}
