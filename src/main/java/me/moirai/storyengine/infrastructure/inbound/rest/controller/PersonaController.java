package me.moirai.storyengine.infrastructure.inbound.rest.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.moirai.storyengine.common.annotation.Authorize;
import me.moirai.storyengine.common.cqs.command.CommandRunner;
import me.moirai.storyengine.common.cqs.query.QueryRunner;
import me.moirai.storyengine.common.dto.PaginatedResult;
import me.moirai.storyengine.common.enums.SearchView;
import me.moirai.storyengine.common.enums.SortDirection;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.common.web.SecurityContextAware;
import me.moirai.storyengine.core.port.inbound.persona.CreatePersona;
import me.moirai.storyengine.core.port.inbound.persona.DeletePersona;
import me.moirai.storyengine.core.port.inbound.persona.GetPersonaById;
import me.moirai.storyengine.core.port.inbound.persona.PersonaDetails;
import me.moirai.storyengine.core.port.inbound.persona.PersonaSortField;
import me.moirai.storyengine.core.port.inbound.persona.PersonaSummary;
import me.moirai.storyengine.core.port.inbound.persona.SearchPersonas;
import me.moirai.storyengine.core.port.inbound.persona.UpdatePersona;
import me.moirai.storyengine.infrastructure.inbound.rest.request.CreatePersonaRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.UpdatePersonaRequest;

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

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PaginatedResult<PersonaSummary> search(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "view", required = true) SearchView view,
            @RequestParam(name = "sorting_field", required = false) PersonaSortField sortingField,
            @RequestParam(name = "direction", required = false) SortDirection direction,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size) {

        return queryRunner.run(new SearchPersonas(
                name,
                view,
                sortingField,
                direction,
                page,
                size,
                getAuthenticatedUser().id()));
    }

    @GetMapping("/{personaId}")
    @ResponseStatus(code = HttpStatus.OK)
    @Authorize(operation = AuthorizationOperation.VIEW_PERSONA, fields = "#personaId")
    public PersonaDetails getPersonaById(
            @PathVariable(required = true) UUID personaId) {

        var query = new GetPersonaById(personaId, authenticatedUserId());
        return queryRunner.run(query);
    }

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public PersonaDetails createPersona(
            @Valid @RequestBody CreatePersonaRequest request) {

        var command = new CreatePersona(
                request.name(),
                request.personality(),
                request.visibility(),
                authenticatedUserId(),
                request.usersAllowedToRead(),
                request.usersAllowedToWrite());

        return commandRunner.run(command);
    }

    @PutMapping("/{personaId}")
    @ResponseStatus(code = HttpStatus.OK)
    @Authorize(operation = AuthorizationOperation.UPDATE_PERSONA, fields = "#personaId")
    public PersonaDetails updatePersona(
            @PathVariable(required = true) UUID personaId,
            @Valid @RequestBody UpdatePersonaRequest request) {

        var command = new UpdatePersona(
                personaId,
                request.name(),
                request.personality(),
                request.visibility(),
                authenticatedUserId(),
                request.usersAllowedToWriteToAdd(),
                request.usersAllowedToReadToAdd(),
                request.usersAllowedToWriteToRemove(),
                request.usersAllowedToReadToRemove());

        return commandRunner.run(command);
    }

    @DeleteMapping("/{personaId}")
    @ResponseStatus(code = HttpStatus.OK)
    @Authorize(operation = AuthorizationOperation.DELETE_PERSONA, fields = "#personaId")
    public void deletePersona(
            @PathVariable(required = true) UUID personaId) {

        var command = new DeletePersona(personaId, authenticatedUserId());
        commandRunner.run(command);
    }
}
