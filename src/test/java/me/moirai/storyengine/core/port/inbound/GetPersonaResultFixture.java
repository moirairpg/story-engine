package me.moirai.storyengine.core.port.inbound;

import me.moirai.storyengine.core.domain.persona.PersonaFixture;
import me.moirai.storyengine.core.port.inbound.persona.PersonaDetails;

public class GetPersonaResultFixture {

    public static PersonaDetails privatePersona() {

        var persona = PersonaFixture.privatePersonaWithIdAndPermissions();
        return new PersonaDetails(
                persona.getPublicId(),
                persona.getName(),
                persona.getPersonality(),
                persona.getVisibility(),
                persona.getPermissions(),
                persona.getCreationDate(),
                persona.getLastUpdateDate());
    }

    public static PersonaDetails publicPersona() {

        var persona = PersonaFixture.publicPersonaWithIdAndPermissions();
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
