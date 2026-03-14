package me.moirai.storyengine.infrastructure.inbound.rest.controller;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.text.CaseUtils.toCamelCase;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.moirai.storyengine.common.usecases.UseCaseRunner;
import me.moirai.storyengine.common.web.SecurityContextAware;
import me.moirai.storyengine.core.port.inbound.persona.AddFavoritePersona;
import me.moirai.storyengine.core.port.inbound.persona.CreatePersona;
import me.moirai.storyengine.core.port.inbound.persona.DeletePersona;
import me.moirai.storyengine.core.port.inbound.persona.GetPersonaById;
import me.moirai.storyengine.core.port.inbound.persona.PersonaDetails;
import me.moirai.storyengine.core.port.inbound.persona.RemoveFavoritePersona;
import me.moirai.storyengine.core.port.inbound.persona.SearchPersonas;
import me.moirai.storyengine.core.port.inbound.persona.SearchPersonasResult;
import me.moirai.storyengine.core.port.inbound.persona.UpdatePersona;
import me.moirai.storyengine.infrastructure.inbound.rest.mapper.PersonaRequestMapper;
import me.moirai.storyengine.infrastructure.inbound.rest.request.CreatePersonaRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.FavoriteRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.PersonaSearchParameters;
import me.moirai.storyengine.infrastructure.inbound.rest.request.UpdatePersonaRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.enums.SearchDirection;
import me.moirai.storyengine.infrastructure.inbound.rest.request.enums.SearchOperation;
import me.moirai.storyengine.infrastructure.inbound.rest.request.enums.SearchSortingField;
import me.moirai.storyengine.infrastructure.inbound.rest.request.enums.SearchVisibility;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/persona")
@Tag(name = "Personas", description = "Endpoints for managing MoirAI Personas")
public class PersonaController extends SecurityContextAware {

    private final UseCaseRunner useCaseRunner;
    private final PersonaRequestMapper requestMapper;

    public PersonaController(UseCaseRunner useCaseRunner,
            PersonaRequestMapper requestMapper) {

        this.useCaseRunner = useCaseRunner;
        this.requestMapper = requestMapper;
    }

    @GetMapping
    @ResponseStatus(code = HttpStatus.OK)
    public Mono<SearchPersonasResult> search(PersonaSearchParameters searchParameters) {

        return mapWithAuthenticatedUser(authenticatedUser -> {

            SearchPersonas query = SearchPersonas.builder()
                    .name(searchParameters.getName())
                    .ownerId(searchParameters.getOwnerId())
                    .favorites(searchParameters.isFavorites())
                    .page(searchParameters.getPage())
                    .size(searchParameters.getSize())
                    .sortingField(getSortingField(searchParameters.getSortingField()))
                    .direction(getDirection(searchParameters.getDirection()))
                    .visibility(getVisibility(searchParameters.getVisibility()))
                    .operation(getOperation(searchParameters.getOperation()))
                    .requesterId(authenticatedUser.getDiscordId())
                    .build();

            return useCaseRunner.run(query);
        });
    }

    @GetMapping("/{personaId}")
    @ResponseStatus(code = HttpStatus.OK)
    @PreAuthorize("canRead(#personaId, 'Persona')")
    public Mono<PersonaDetails> getPersonaById(@PathVariable(required = true) String personaId) {

        return mapWithAuthenticatedUser(authenticatedUser -> {

            GetPersonaById query = GetPersonaById.build(personaId, authenticatedUser.getDiscordId());
            return useCaseRunner.run(query);
        });
    }

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public Mono<PersonaDetails> createPersona(@Valid @RequestBody CreatePersonaRequest request) {

        return flatMapWithAuthenticatedUser(authenticatedUser -> {

            CreatePersona command = requestMapper.toCommand(request, authenticatedUser.getDiscordId());
            return useCaseRunner.run(command);
        });
    }

    @PutMapping("/{personaId}")
    @ResponseStatus(code = HttpStatus.OK)
    @PreAuthorize("canWrite(#personaId, 'Persona')")
    public Mono<PersonaDetails> updatePersona(
            @PathVariable(required = true) String personaId,
            @Valid @RequestBody UpdatePersonaRequest request) {

        return flatMapWithAuthenticatedUser(authenticatedUser -> {

            UpdatePersona command = requestMapper.toCommand(request, personaId,
                    authenticatedUser.getDiscordId());

            return useCaseRunner.run(command);
        });
    }

    @DeleteMapping("/{personaId}")
    @ResponseStatus(code = HttpStatus.OK)
    @PreAuthorize("canWrite(#personaId, 'Persona')")
    public Mono<Void> deletePersona(@PathVariable(required = true) String personaId) {

        return flatMapWithAuthenticatedUser(authenticatedUser -> {

            DeletePersona command = DeletePersona.build(personaId, authenticatedUser.getDiscordId());
            useCaseRunner.run(command);

            return Mono.empty();
        });
    }

    @PostMapping("/favorite")
    @ResponseStatus(code = HttpStatus.CREATED)
    @PreAuthorize("canRead(#request.assetId, 'Persona')")
    public Mono<Void> addFavoritePersona(@RequestBody FavoriteRequest request) {

        return flatMapWithAuthenticatedUser(authenticatedUser -> {

            AddFavoritePersona command = AddFavoritePersona.builder()
                    .assetId(request.getAssetId())
                    .playerId(authenticatedUser.getDiscordId())
                    .build();

            useCaseRunner.run(command);

            return Mono.empty();
        });
    }

    @DeleteMapping("/favorite/{assetId}")
    @ResponseStatus(code = HttpStatus.CREATED)
    public Mono<Void> removeFavoritePersona(@PathVariable(required = true) String assetId) {

        return flatMapWithAuthenticatedUser(authenticatedUser -> {

            RemoveFavoritePersona command = RemoveFavoritePersona.builder()
                    .assetId(assetId)
                    .playerId(authenticatedUser.getDiscordId())
                    .build();

            useCaseRunner.run(command);

            return Mono.empty();
        });
    }

    private String getSortingField(SearchSortingField searchSortingField) {

        if (searchSortingField != null) {
            return toCamelCase(searchSortingField.name(), false, '_');
        }

        return EMPTY;
    }

    private String getDirection(SearchDirection searchDirection) {

        if (searchDirection != null) {
            return toCamelCase(searchDirection.name(), false, '_');
        }

        return EMPTY;
    }

    private String getVisibility(SearchVisibility searchVisibility) {

        if (searchVisibility != null) {
            return toCamelCase(searchVisibility.name(), false, '_');
        }

        return EMPTY;
    }

    private String getOperation(SearchOperation searchOperation) {

        if (searchOperation != null) {
            return toCamelCase(searchOperation.name(), false, '_');
        }

        return EMPTY;
    }
}
