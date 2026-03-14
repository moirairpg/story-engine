package me.moirai.storyengine.infrastructure.inbound.rest.mapper;

import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

import org.springframework.stereotype.Component;

import me.moirai.storyengine.core.application.usecase.persona.request.CreatePersona;
import me.moirai.storyengine.core.application.usecase.persona.request.UpdatePersona;
import me.moirai.storyengine.infrastructure.inbound.rest.request.CreatePersonaRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.UpdatePersonaRequest;

@Component
public class PersonaRequestMapper {

    public CreatePersona toCommand(CreatePersonaRequest request, String requesterId) {

        return CreatePersona.builder()
                .name(request.getName())
                .personality(request.getPersonality())
                .visibility(request.getVisibility())
                .requesterId(requesterId)
                .usersAllowedToRead(emptyIfNull(request.getUsersAllowedToRead())
                        .stream()
                        .collect(toSet()))
                .usersAllowedToWrite(emptyIfNull(request.getUsersAllowedToWrite())
                        .stream()
                        .collect(toSet()))
                .build();
    }

    public UpdatePersona toCommand(UpdatePersonaRequest request, String personaId, String requesterId) {

        return UpdatePersona.builder()
                .id(personaId)
                .name(request.getName())
                .personality(request.getPersonality())
                .visibility(request.getVisibility())
                .requesterId(requesterId)
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
