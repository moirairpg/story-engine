package me.moirai.storyengine.infrastructure.inbound.rest.response;

import me.moirai.storyengine.core.domain.persona.Persona;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;

public class PersonaResponseFixture {

    public static PersonaResponse.Builder publicPersona() {

        Persona persona = PersonaFixture.publicPersona().build();
        return PersonaResponse.builder()
                .id(persona.getId())
                .name(persona.getName())
                .personality(persona.getPersonality())
                .visibility(persona.getVisibility().toString())
                .ownerId(persona.getOwnerId())
                .usersAllowedToRead(persona.getUsersAllowedToRead())
                .usersAllowedToWrite(persona.getUsersAllowedToWrite());
    }

    public static PersonaResponse.Builder privatePersona() {

        Persona persona = PersonaFixture.privatePersona().build();
        return PersonaResponse.builder()
                .id(persona.getId())
                .name(persona.getName())
                .personality(persona.getPersonality())
                .visibility(persona.getVisibility().toString())
                .ownerId(persona.getOwnerId())
                .usersAllowedToRead(persona.getUsersAllowedToRead())
                .usersAllowedToWrite(persona.getUsersAllowedToWrite());
    }
}
