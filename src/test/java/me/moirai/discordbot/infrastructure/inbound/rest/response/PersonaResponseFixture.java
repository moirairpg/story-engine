package me.moirai.discordbot.infrastructure.inbound.rest.response;

import me.moirai.discordbot.core.domain.persona.Persona;
import me.moirai.discordbot.core.domain.persona.PersonaFixture;

public class PersonaResponseFixture {

    public static PersonaResponse.Builder publicPersona() {

        Persona persona = PersonaFixture.publicPersona().build();
        return PersonaResponse.builder()
                .id(persona.getId())
                .name(persona.getName())
                .personality(persona.getPersonality())
                .visibility(persona.getVisibility().toString())
                .ownerDiscordId(persona.getOwnerDiscordId())
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
                .ownerDiscordId(persona.getOwnerDiscordId())
                .usersAllowedToRead(persona.getUsersAllowedToRead())
                .usersAllowedToWrite(persona.getUsersAllowedToWrite());
    }
}
