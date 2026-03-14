package me.moirai.storyengine.infrastructure.inbound.rest.mapper;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import me.moirai.storyengine.core.port.inbound.persona.CreatePersonaResult;
import me.moirai.storyengine.core.port.inbound.persona.GetPersonaResult;
import me.moirai.storyengine.core.port.inbound.persona.SearchPersonasResult;
import me.moirai.storyengine.core.port.inbound.persona.UpdatePersonaResult;
import me.moirai.storyengine.infrastructure.inbound.rest.response.CreatePersonaResponse;
import me.moirai.storyengine.infrastructure.inbound.rest.response.PersonaResponse;
import me.moirai.storyengine.infrastructure.inbound.rest.response.SearchPersonasResponse;
import me.moirai.storyengine.infrastructure.inbound.rest.response.UpdatePersonaResponse;

@Component
public class PersonaResponseMapper {

    public SearchPersonasResponse toResponse(SearchPersonasResult result) {

        List<PersonaResponse> personas = CollectionUtils.emptyIfNull(result.getResults())
                .stream()
                .map(this::toResponse)
                .toList();

        return SearchPersonasResponse.builder()
                .page(result.getPage())
                .resultsInPage(result.getItems())
                .totalPages(result.getTotalPages())
                .totalResults(result.getTotalItems())
                .results(personas)
                .build();
    }

    public PersonaResponse toResponse(GetPersonaResult result) {

        return PersonaResponse.builder()
                .id(result.getId())
                .name(result.getName())
                .personality(result.getPersonality())
                .creationDate(result.getCreationDate())
                .lastUpdateDate(result.getLastUpdateDate())
                .visibility(result.getVisibility())
                .ownerId(result.getOwnerId())
                .build();
    }

    public CreatePersonaResponse toResponse(CreatePersonaResult result) {

        return CreatePersonaResponse.build(result.getId());
    }

    public UpdatePersonaResponse toResponse(UpdatePersonaResult result) {

        return UpdatePersonaResponse.build(result.getLastUpdatedDateTime());
    }
}
