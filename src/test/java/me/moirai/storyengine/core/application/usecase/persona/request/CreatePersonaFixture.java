package me.moirai.storyengine.core.application.usecase.persona.request;

import me.moirai.storyengine.core.port.inbound.persona.CreatePersona;

import me.moirai.storyengine.core.domain.persona.Persona;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;

public class CreatePersonaFixture {

    public static CreatePersona createPrivatePersona() {

        Persona persona = PersonaFixture.privatePersona().build();
        return new CreatePersona(
                persona.getName(),
                persona.getPersonality(),
                persona.getVisibility(),
                persona.getOwnerId(),
                persona.getUsersAllowedToRead(),
                persona.getUsersAllowedToWrite());
    }
}
