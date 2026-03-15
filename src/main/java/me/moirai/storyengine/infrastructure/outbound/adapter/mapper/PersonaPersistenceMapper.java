package me.moirai.storyengine.infrastructure.outbound.adapter.mapper;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import me.moirai.storyengine.core.port.inbound.persona.PersonaDetails;
import me.moirai.storyengine.core.port.inbound.persona.SearchPersonasResult;
import me.moirai.storyengine.core.domain.persona.Persona;

@Component
public class PersonaPersistenceMapper {

    public PersonaDetails mapToResult(Persona persona) {

        return PersonaDetails.builder()
                .id(persona.getPublicId())
                .name(persona.getName())
                .personality(persona.getPersonality())
                .visibility(persona.getVisibility().name())
                .usersAllowedToRead(persona.getUsersAllowedToRead())
                .usersAllowedToWrite(persona.getUsersAllowedToWrite())
                .creationDate(persona.getCreationDate())
                .lastUpdateDate(persona.getLastUpdateDate())
                .ownerId(persona.getOwnerId())
                .build();
    }

    public SearchPersonasResult mapToResult(Page<Persona> pagedResult) {

        return SearchPersonasResult.builder()
                .results(pagedResult.getContent()
                        .stream()
                        .map(this::mapToResult)
                        .toList())
                .page(pagedResult.getNumber() + 1)
                .items(pagedResult.getNumberOfElements())
                .totalItems(pagedResult.getTotalElements())
                .totalPages(pagedResult.getTotalPages())
                .build();
    }
}
