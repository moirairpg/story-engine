package me.moirai.storyengine.core.application.usecase.persona.result;

import me.moirai.storyengine.core.port.inbound.persona.PersonaDetails;

import me.moirai.storyengine.core.domain.persona.Persona;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;

public class GetPersonaResultFixture {

    public static PersonaDetails.Builder privatePersona() {

        Persona persona = PersonaFixture.privatePersona().build();
        return PersonaDetails.builder()
                .id(persona.getPublicId())
                .name(persona.getName())
                .personality(persona.getPersonality())
                .visibility(persona.getVisibility().name())
                .usersAllowedToRead(persona.getUsersAllowedToRead())
                .usersAllowedToWrite(persona.getUsersAllowedToWrite())
                .creationDate(persona.getCreationDate())
                .lastUpdateDate(persona.getLastUpdateDate())
                .ownerId(persona.getOwnerId());
    }

    public static PersonaDetails.Builder publicPersona() {

        Persona persona = PersonaFixture.publicPersona().build();
        return PersonaDetails.builder()
                .id(persona.getPublicId())
                .name(persona.getName())
                .personality(persona.getPersonality())
                .visibility(persona.getVisibility().name())
                .usersAllowedToRead(persona.getUsersAllowedToRead())
                .usersAllowedToWrite(persona.getUsersAllowedToWrite())
                .creationDate(persona.getCreationDate())
                .lastUpdateDate(persona.getLastUpdateDate())
                .ownerId(persona.getOwnerId());
    }
}
