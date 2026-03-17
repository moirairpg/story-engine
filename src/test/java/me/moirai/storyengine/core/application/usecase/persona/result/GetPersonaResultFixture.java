package me.moirai.storyengine.core.application.usecase.persona.result;

import me.moirai.storyengine.core.port.inbound.persona.PersonaDetails;

import me.moirai.storyengine.core.domain.persona.Persona;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;

public class GetPersonaResultFixture {

    public static PersonaDetails privatePersona() {

        Persona persona = PersonaFixture.privatePersona().build();
        return new PersonaDetails(
                persona.getPublicId(),
                persona.getName(),
                persona.getPersonality(),
                persona.getVisibility(),
                persona.getOwnerId(),
                persona.getUsersAllowedToWrite(),
                persona.getUsersAllowedToRead(),
                persona.getCreationDate(),
                persona.getLastUpdateDate());
    }

    public static PersonaDetails publicPersona() {

        Persona persona = PersonaFixture.publicPersona().build();
        return new PersonaDetails(
                persona.getPublicId(),
                persona.getName(),
                persona.getPersonality(),
                persona.getVisibility(),
                persona.getOwnerId(),
                persona.getUsersAllowedToWrite(),
                persona.getUsersAllowedToRead(),
                persona.getCreationDate(),
                persona.getLastUpdateDate());
    }
}
