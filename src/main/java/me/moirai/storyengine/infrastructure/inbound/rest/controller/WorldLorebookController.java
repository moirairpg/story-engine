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
import me.moirai.storyengine.core.port.inbound.world.CreateWorldLorebookEntry;
import me.moirai.storyengine.core.port.inbound.world.DeleteWorldLorebookEntry;
import me.moirai.storyengine.core.port.inbound.world.GetWorldLorebookEntryById;
import me.moirai.storyengine.core.port.inbound.world.SearchWorldLorebookEntries;
import me.moirai.storyengine.core.port.inbound.world.SearchWorldLorebookEntriesResult;
import me.moirai.storyengine.core.port.inbound.world.UpdateWorldLorebookEntry;
import me.moirai.storyengine.core.port.inbound.world.WorldLorebookEntryDetails;
import me.moirai.storyengine.infrastructure.inbound.rest.request.LorebookSearchParameters;
import me.moirai.storyengine.infrastructure.inbound.rest.request.WorldLorebookEntryRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.enums.SearchDirection;
import me.moirai.storyengine.infrastructure.inbound.rest.request.enums.SearchSortingField;
import reactor.core.publisher.Mono;

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

    // TODO reform search request
    @GetMapping
    @ResponseStatus(code = HttpStatus.OK)
    public Mono<SearchWorldLorebookEntriesResult> search(
            @PathVariable(required = true) UUID worldId,
            LorebookSearchParameters searchParameters) {

        return mapWithAuthenticatedUser(authenticatedUser -> {

            var query = new SearchWorldLorebookEntries(
                    searchParameters.getName(),
                    worldId,
                    searchParameters.getPage(),
                    searchParameters.getSize(),
                    getSortingField(searchParameters.getSortingField()),
                    getDirection(searchParameters.getDirection()),
                    authenticatedUser.discordId());

            return queryRunner.run(query);
        });
    }

    @GetMapping("/{entryId}")
    @ResponseStatus(code = HttpStatus.OK)
    public Mono<WorldLorebookEntryDetails> getLorebookEntryById(
            @PathVariable(required = true) UUID worldId,
            @PathVariable(required = true) UUID entryId) {

        return mapWithAuthenticatedUser(authenticatedUser -> {

            var query = new GetWorldLorebookEntryById(
                    entryId,
                    worldId,
                    authenticatedUser.discordId());

            return queryRunner.run(query);
        });
    }

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public Mono<WorldLorebookEntryDetails> createLorebookEntry(
            @PathVariable(required = true) UUID worldId,
            @Valid @RequestBody WorldLorebookEntryRequest request) {

        return mapWithAuthenticatedUser(authenticatedUser -> {

            var command = new CreateWorldLorebookEntry(
                    worldId,
                    request.name(),
                    request.description(),
                    request.regex(),
                    authenticatedUser.discordId());

            return commandRunner.run(command);
        });
    }

    @PutMapping("/{entryId}")
    @ResponseStatus(code = HttpStatus.OK)
    public Mono<WorldLorebookEntryDetails> updateLorebookEntry(
            @PathVariable(required = true) UUID worldId,
            @PathVariable(required = true) UUID entryId,
            @Valid @RequestBody WorldLorebookEntryRequest request) {

        return mapWithAuthenticatedUser(authenticatedUser -> {

            var command = new UpdateWorldLorebookEntry(
                    entryId,
                    worldId,
                    request.name(),
                    request.description(),
                    request.regex(),
                    authenticatedUser.discordId());

            return commandRunner.run(command);
        });
    }

    @DeleteMapping("/{entryId}")
    @ResponseStatus(code = HttpStatus.OK)
    public Mono<Void> deleteLorebookEntry(
            @PathVariable(required = true) UUID worldId,
            @PathVariable(required = true) UUID entryId) {

        return flatMapWithAuthenticatedUser(authenticatedUser -> {

            var command = new DeleteWorldLorebookEntry(
                    entryId,
                    worldId,
                    authenticatedUser.discordId());

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
}
