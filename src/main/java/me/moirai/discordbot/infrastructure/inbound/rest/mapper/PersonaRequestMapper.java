package me.moirai.discordbot.infrastructure.inbound.rest.mapper;

import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

import org.springframework.stereotype.Component;

import me.moirai.discordbot.core.application.usecase.persona.request.CreatePersona;
import me.moirai.discordbot.core.application.usecase.persona.request.UpdatePersona;
import me.moirai.discordbot.infrastructure.inbound.rest.request.CreatePersonaRequest;
import me.moirai.discordbot.infrastructure.inbound.rest.request.UpdatePersonaRequest;

@Component
public class PersonaRequestMapper {

    public CreatePersona toCommand(CreatePersonaRequest request, String requesterDiscordId) {

        return CreatePersona.builder()
                .name(request.getName())
                .personality(request.getPersonality())
                .visibility(request.getVisibility())
                .requesterDiscordId(requesterDiscordId)
                .usersAllowedToRead(emptyIfNull(request.getUsersAllowedToRead())
                        .stream()
                        .collect(toSet()))
                .usersAllowedToWrite(emptyIfNull(request.getUsersAllowedToWrite())
                        .stream()
                        .collect(toSet()))
                .build();
    }

    public UpdatePersona toCommand(UpdatePersonaRequest request, String personaId, String requesterDiscordId) {

        return UpdatePersona.builder()
                .id(personaId)
                .name(request.getName())
                .personality(request.getPersonality())
                .visibility(request.getVisibility())
                .requesterDiscordId(requesterDiscordId)
                .usersAllowedToWriteToAdd(emptyIfNull(request.getUsersAllowedToWriteToAdd())
                        .stream()
                        .collect(toSet()))
                .usersAllowedToReadToAdd(emptyIfNull(request.getUsersAllowedToReadToAdd())
                        .stream()
                        .collect(toSet()))
                .usersAllowedToWriteToRemove(emptyIfNull(request.getUsersAllowedToWriteToRemove())
                        .stream()
                        .collect(toSet()))
                .usersAllowedToReadToRemove(emptyIfNull(request.getUsersAllowedToReadToRemove())
                        .stream()
                        .collect(toSet()))
                .build();
    }
}
