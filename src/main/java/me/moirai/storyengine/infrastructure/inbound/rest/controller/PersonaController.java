package me.moirai.storyengine.infrastructure.inbound.rest.controller;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.text.CaseUtils.toCamelCase;

import java.util.UUID;

import org.springframework.http.HttpStatus;
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
import me.moirai.storyengine.common.cqs.command.CommandRunner;
import me.moirai.storyengine.common.cqs.query.QueryRunner;
import me.moirai.storyengine.common.web.SecurityContextAware;
import me.moirai.storyengine.core.port.inbound.persona.CreatePersona;
import me.moirai.storyengine.core.port.inbound.persona.DeletePersona;
import me.moirai.storyengine.core.port.inbound.persona.GetPersonaById;
import me.moirai.storyengine.core.port.inbound.persona.PersonaDetails;
import me.moirai.storyengine.core.port.inbound.persona.SearchPersonas;
import me.moirai.storyengine.core.port.inbound.persona.SearchPersonasResult;
import me.moirai.storyengine.core.port.inbound.persona.UpdatePersona;
import me.moirai.storyengine.infrastructure.inbound.rest.request.CreatePersonaRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.PersonaSearchParameters;
import me.moirai.storyengine.infrastructure.inbound.rest.request.UpdatePersonaRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.enums.SearchDirection;
import me.moirai.storyengine.infrastructure.inbound.rest.request.enums.SearchOperation;
import me.moirai.storyengine.infrastructure.inbound.rest.request.enums.SearchSortingField;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/persona")
@Tag(name = "Personas", description = "Endpoints for managing MoirAI Personas")
public class PersonaController extends SecurityContextAware {

    private final QueryRunner queryRunner;
    private final CommandRunner commandRunner;

    public PersonaController(
            QueryRunner queryRunner,
            CommandRunner commandRunner) {

        this.queryRunner = queryRunner;
        this.commandRunner = commandRunner;
    }

    // TODO reform search request
    @GetMapping
    @ResponseStatus(code = HttpStatus.OK)
    public Mono<SearchPersonasResult> search(PersonaSearchParameters searchParameters) {

        return mapWithAuthenticatedUser(authenticatedUser -> {

            var query = new SearchPersonas(
                    searchParameters.getName(),
                    searchParameters.getOwnerId(),
                    searchParameters.getPage(),
                    searchParameters.getSize(),
                    getSortingField(searchParameters.getSortingField()),
                    getDirection(searchParameters.getDirection()),
                    searchParameters.getVisibility(),
                    getOperation(searchParameters.getOperation()),
                    authenticatedUser.getDiscordId());

            return queryRunner.run(query);
        });
    }

    @GetMapping("/{personaId}")
    @ResponseStatus(code = HttpStatus.OK)
    public Mono<PersonaDetails> getPersonaById(@PathVariable(required = true) UUID personaId) {

        return mapWithAuthenticatedUser(authenticatedUser -> {

            var query = new GetPersonaById(personaId, authenticatedUser.getDiscordId());
            return queryRunner.run(query);
        });
    }

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public Mono<PersonaDetails> createPersona(@Valid @RequestBody CreatePersonaRequest request) {

        return flatMapWithAuthenticatedUser(authenticatedUser -> {

            var command = new CreatePersona(
                    request.name(),
                    request.personality(),
                    request.visibility(),
                    authenticatedUser.getDiscordId(),
                    request.usersAllowedToRead(),
                    request.usersAllowedToWrite());

            return commandRunner.run(command);
        });
    }

    @PutMapping("/{personaId}")
    @ResponseStatus(code = HttpStatus.OK)
    public Mono<PersonaDetails> updatePersona(
            @PathVariable(required = true) UUID personaId,
            @Valid @RequestBody UpdatePersonaRequest request) {

        return flatMapWithAuthenticatedUser(authenticatedUser -> {

            var command = new UpdatePersona(
                    personaId,
                    request.name(),
                    request.personality(),
                    request.visibility(),
                    authenticatedUser.getDiscordId(),
                    request.usersAllowedToWriteToAdd(),
                    request.usersAllowedToReadToAdd(),
                    request.usersAllowedToWriteToRemove(),
                    request.usersAllowedToReadToRemove());

            return commandRunner.run(command);
        });
    }

    @DeleteMapping("/{personaId}")
    @ResponseStatus(code = HttpStatus.OK)
    public Mono<Void> deletePersona(@PathVariable(required = true) UUID personaId) {

        return flatMapWithAuthenticatedUser(authenticatedUser -> {

            var command = new DeletePersona(personaId, authenticatedUser.getDiscordId());
            commandRunner.run(command);

            return Mono.empty();
        });
    }

    // TODO remove all of this
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

    private String getOperation(SearchOperation searchOperation) {

        if (searchOperation != null) {
            return toCamelCase(searchOperation.name(), false, '_');
        }

        return EMPTY;
    }
}
