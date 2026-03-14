package me.moirai.storyengine.core.application.usecase.persona.result;

import me.moirai.storyengine.core.port.inbound.persona.GetPersonaResult;

import me.moirai.storyengine.core.domain.persona.Persona;
import me.moirai.storyengine.core.domain.persona.PersonaFixture;

public class GetPersonaResultFixture {

    public static GetPersonaResult.Builder privatePersona() {

        Persona persona = PersonaFixture.privatePersona().build();
        return GetPersonaResult.builder()
                .id(persona.getId())
                .name(persona.getName())
                .personality(persona.getPersonality())
                .visibility(persona.getVisibility().name())
                .usersAllowedToRead(persona.getUsersAllowedToRead())
                .usersAllowedToWrite(persona.getUsersAllowedToWrite())
                .creationDate(persona.getCreationDate())
                .lastUpdateDate(persona.getLastUpdateDate())
                .ownerId(persona.getOwnerId());
    }

    public static GetPersonaResult.Builder publicPersona() {

        Persona persona = PersonaFixture.publicPersona().build();
        return GetPersonaResult.builder()
                .id(persona.getId())
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
