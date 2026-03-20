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
import me.moirai.storyengine.common.enums.SortDirection;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.common.web.SecurityContextAware;
import me.moirai.storyengine.core.port.inbound.LorebookEntrySummary;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureLorebookEntryDetails;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureLorebookSortField;
import me.moirai.storyengine.core.port.inbound.adventure.CreateAdventureLorebookEntry;
import me.moirai.storyengine.core.port.inbound.adventure.DeleteAdventureLorebookEntry;
import me.moirai.storyengine.core.port.inbound.adventure.GetAdventureLorebookEntryById;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventureLorebookEntries;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventureLorebookEntry;
import me.moirai.storyengine.infrastructure.inbound.rest.request.AdventureLorebookEntryRequest;

@RestController
@RequestMapping("/adventure/{adventureId}/lorebook")
@Tag(name = "Adventure Lorebooks", description = "Endpoints for managing MoirAI Adventure Lorebooks")
public class AdventureLorebookController extends SecurityContextAware {

    private final QueryRunner queryRunner;
    private final CommandRunner commandRunner;

    public AdventureLorebookController(
            QueryRunner queryRunner,
            CommandRunner commandRunner) {

        this.queryRunner = queryRunner;
        this.commandRunner = commandRunner;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PaginatedResult<LorebookEntrySummary> search(
            @PathVariable UUID adventureId,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "sorting_field", required = false) AdventureLorebookSortField sortingField,
            @RequestParam(name = "direction", required = false) SortDirection direction,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size) {

        return queryRunner.run(new SearchAdventureLorebookEntries(
                adventureId,
                name,
                sortingField,
                direction,
                page,
                size,
                authenticatedUserId()));
    }

    @GetMapping("/{entryId}")
    @ResponseStatus(code = HttpStatus.OK)
    @Authorize(operation = AuthorizationOperation.VIEW_ADVENTURE, fields = "#adventureId")
    public AdventureLorebookEntryDetails getLorebookEntryById(
            @PathVariable(required = true) UUID adventureId,
            @PathVariable(required = true) UUID entryId) {

        var query = new GetAdventureLorebookEntryById(
                entryId,
                adventureId,
                authenticatedUserId());

        return queryRunner.run(query);
    }

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    @Authorize(operation = AuthorizationOperation.UPDATE_ADVENTURE, fields = "#adventureId")
    public AdventureLorebookEntryDetails createLorebookEntry(
            @PathVariable(required = true) UUID adventureId,
            @Valid @RequestBody AdventureLorebookEntryRequest request) {

        var command = new CreateAdventureLorebookEntry(
                adventureId,
                request.name(),
                request.regex(),
                request.description(),
                request.playerId(),
                authenticatedUserId());

        return commandRunner.run(command);
    }

    @PutMapping("/{entryId}")
    @ResponseStatus(code = HttpStatus.OK)
    @Authorize(operation = AuthorizationOperation.UPDATE_ADVENTURE, fields = "#adventureId")
    public AdventureLorebookEntryDetails updateLorebookEntry(
            @PathVariable(required = true) UUID adventureId,
            @PathVariable(required = true) UUID entryId,
            @Valid @RequestBody AdventureLorebookEntryRequest request) {

        var command = new UpdateAdventureLorebookEntry(
                entryId,
                adventureId,
                request.name(),
                request.regex(),
                request.description(),
                request.playerId(),
                authenticatedUserId());

        return commandRunner.run(command);
    }

    @DeleteMapping("/{entryId}")
    @ResponseStatus(code = HttpStatus.OK)
    @Authorize(operation = AuthorizationOperation.UPDATE_ADVENTURE, fields = "#adventureId")
    public void deleteLorebookEntry(
            @PathVariable(required = true) UUID adventureId,
            @PathVariable(required = true) UUID entryId) {

        var command = new DeleteAdventureLorebookEntry(
                entryId,
                adventureId,
                authenticatedUserId());

        commandRunner.run(command);
    }

}
