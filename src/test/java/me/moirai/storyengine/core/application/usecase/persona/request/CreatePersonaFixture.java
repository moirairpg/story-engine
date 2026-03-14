package me.moirai.storyengine.core.application.usecase.persona.request;

import me.moirai.storyengine.core.port.inbound.persona.CreatePersona;

import me.moirai.storyengine.core.domain.persona.Persona;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;

public class CreatePersonaFixture {

    public static CreatePersona.Builder createPrivatePersona() {

        Persona persona = PersonaFixture.privatePersona().build();
        return CreatePersona.builder()
                .name(persona.getName())
                .personality(persona.getPersonality())
                .visibility(persona.getVisibility().toString())
                .requesterId(persona.getOwnerId())
                .usersAllowedToWrite(persona.getUsersAllowedToRead())
                .usersAllowedToRead(persona.getUsersAllowedToWrite());
    }
}
