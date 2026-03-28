package me.moirai.storyengine.core.port.inbound;

import java.util.Set;

import me.moirai.storyengine.core.port.inbound.persona.CreatePersona;

import me.moirai.storyengine.core.domain.persona.PersonaFixture;

public class CreatePersonaFixture {

    public static CreatePersona createPrivatePersona() {

        var persona = PersonaFixture.privatePersona().build();
        return new CreatePersona(
                persona.getName(),
                persona.getPersonality(),
                persona.getVisibility(),
                Set.of());
    }
}
