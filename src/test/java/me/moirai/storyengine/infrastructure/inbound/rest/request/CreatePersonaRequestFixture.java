package me.moirai.storyengine.infrastructure.inbound.rest.request;

import me.moirai.storyengine.core.domain.persona.Persona;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;

public class CreatePersonaRequestFixture {

    public static CreatePersonaRequest createPrivatePersona() {

        Persona persona = PersonaFixture.privatePersona().build();
        CreatePersonaRequest request = new CreatePersonaRequest();

        request.setName(persona.getName());
        request.setPersonality(persona.getPersonality());
        request.setVisibility(persona.getVisibility().toString());
        request.setUsersAllowedToRead(persona.getUsersAllowedToRead());
        request.setUsersAllowedToWrite(persona.getUsersAllowedToWrite());

        return request;
    }
}
