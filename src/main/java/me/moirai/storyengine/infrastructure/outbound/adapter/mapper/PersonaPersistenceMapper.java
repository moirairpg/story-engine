package me.moirai.storyengine.infrastructure.outbound.adapter.mapper;

import org.springframework.stereotype.Component;

import me.moirai.storyengine.core.port.inbound.persona.PersonaDetails;
import me.moirai.storyengine.core.domain.persona.Persona;

@Component
public class PersonaPersistenceMapper {

    public PersonaDetails mapToResult(Persona persona) {

        return new PersonaDetails(
                persona.getPublicId(),
                persona.getName(),
                persona.getPersonality(),
                persona.getVisibility(),
                persona.getPermissions(),
                persona.getCreationDate(),
                persona.getLastUpdateDate());
    }
}
