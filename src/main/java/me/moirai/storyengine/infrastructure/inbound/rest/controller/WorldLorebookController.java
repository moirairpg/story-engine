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
import me.moirai.storyengine.core.port.inbound.world.CreateWorldLorebookEntry;
import me.moirai.storyengine.core.port.inbound.world.DeleteWorldLorebookEntry;
import me.moirai.storyengine.core.port.inbound.world.GetWorldLorebookEntryById;
import me.moirai.storyengine.core.port.inbound.world.SearchWorldLorebookEntries;
import me.moirai.storyengine.core.port.inbound.world.UpdateWorldLorebookEntry;
import me.moirai.storyengine.core.port.inbound.world.WorldLorebookEntryDetails;
import me.moirai.storyengine.core.port.inbound.world.WorldLorebookSortField;
import me.moirai.storyengine.infrastructure.inbound.rest.request.WorldLorebookEntryRequest;

@RestController
@RequestMapping("/world/{worldId}/lorebook")
@Tag(name = "World Lorebooks", description = "Endpoints for managing MoirAI World Lorebooks")
public class WorldLorebookController extends SecurityContextAware {

    private final QueryRunner queryRunner;
    private final CommandRunner commandRunner;

    public WorldLorebookController(
            QueryRunner queryRunner,
            CommandRunner commandRunner) {

        this.queryRunner = queryRunner;
        this.commandRunner = commandRunner;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PaginatedResult<LorebookEntrySummary> search(
            @PathVariable UUID worldId,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "sorting_field", required = false) WorldLorebookSortField sortingField,
            @RequestParam(name = "direction", required = false) SortDirection direction,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size) {

        return queryRunner.run(new SearchWorldLorebookEntries(
                worldId,
                name,
                sortingField,
                direction,
                page,
                size,
                authenticatedUserId()));
    }

    @GetMapping("/{entryId}")
    @ResponseStatus(code = HttpStatus.OK)
    @Authorize(operation = AuthorizationOperation.VIEW_WORLD, fields = "#worldId")
    public WorldLorebookEntryDetails getLorebookEntryById(
            @PathVariable(required = true) UUID worldId,
            @PathVariable(required = true) UUID entryId) {

        var query = new GetWorldLorebookEntryById(
                entryId,
                worldId,
                authenticatedUserId());

        return queryRunner.run(query);
    }

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    @Authorize(operation = AuthorizationOperation.UPDATE_WORLD, fields = "#worldId")
    public WorldLorebookEntryDetails createLorebookEntry(
            @PathVariable(required = true) UUID worldId,
            @Valid @RequestBody WorldLorebookEntryRequest request) {

        var command = new CreateWorldLorebookEntry(
                worldId,
                request.name(),
                request.regex(),
                request.description(),
                authenticatedUserId());

        return commandRunner.run(command);
    }

    @PutMapping("/{entryId}")
    @ResponseStatus(code = HttpStatus.OK)
    @Authorize(operation = AuthorizationOperation.UPDATE_WORLD, fields = "#worldId")
    public WorldLorebookEntryDetails updateLorebookEntry(
            @PathVariable(required = true) UUID worldId,
            @PathVariable(required = true) UUID entryId,
            @Valid @RequestBody WorldLorebookEntryRequest request) {

        var command = new UpdateWorldLorebookEntry(
                entryId,
                worldId,
                request.name(),
                request.regex(),
                request.description(),
                authenticatedUserId());

        return commandRunner.run(command);
    }

    @DeleteMapping("/{entryId}")
    @ResponseStatus(code = HttpStatus.OK)
    @Authorize(operation = AuthorizationOperation.UPDATE_WORLD, fields = "#worldId")
    public void deleteLorebookEntry(
            @PathVariable(required = true) UUID worldId,
            @PathVariable(required = true) UUID entryId) {

        var command = new DeleteWorldLorebookEntry(
                entryId,
                worldId,
                authenticatedUserId());

        commandRunner.run(command);
    }
}
