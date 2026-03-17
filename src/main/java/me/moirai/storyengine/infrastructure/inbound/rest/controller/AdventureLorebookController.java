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
import me.moirai.storyengine.core.port.inbound.adventure.AdventureLorebookEntryDetails;
import me.moirai.storyengine.core.port.inbound.adventure.CreateAdventureLorebookEntry;
import me.moirai.storyengine.core.port.inbound.adventure.DeleteAdventureLorebookEntry;
import me.moirai.storyengine.core.port.inbound.adventure.GetAdventureLorebookEntryById;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventureLorebookEntries;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventureLorebookEntriesResult;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventureLorebookEntry;
import me.moirai.storyengine.infrastructure.inbound.rest.request.AdventureLorebookEntryRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.LorebookSearchParameters;
import me.moirai.storyengine.infrastructure.inbound.rest.request.enums.SearchDirection;
import me.moirai.storyengine.infrastructure.inbound.rest.request.enums.SearchSortingField;
import reactor.core.publisher.Mono;

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
    @ResponseStatus(code = HttpStatus.OK)
    public Mono<SearchAdventureLorebookEntriesResult> search(
            @PathVariable(required = true) UUID adventureId,
            LorebookSearchParameters searchParameters) {

        return mapWithAuthenticatedUser(authenticatedUser -> {

            SearchAdventureLorebookEntries query = new SearchAdventureLorebookEntries(
                    adventureId,
                    searchParameters.getName(),
                    searchParameters.getPage(),
                    searchParameters.getSize(),
                    getSortingField(searchParameters.getSortingField()),
                    getDirection(searchParameters.getDirection()),
                    authenticatedUser.getDiscordId());

            return queryRunner.run(query);
        });
    }

    @GetMapping("/{entryId}")
    @ResponseStatus(code = HttpStatus.OK)
    public Mono<AdventureLorebookEntryDetails> getLorebookEntryById(
            @PathVariable(required = true) UUID adventureId,
            @PathVariable(required = true) UUID entryId) {

        return mapWithAuthenticatedUser(authenticatedUser -> {

            GetAdventureLorebookEntryById query = new GetAdventureLorebookEntryById(
                    entryId,
                    adventureId,
                    authenticatedUser.getDiscordId());

            return queryRunner.run(query);
        });
    }

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public Mono<AdventureLorebookEntryDetails> createLorebookEntry(
            @PathVariable(required = true) UUID adventureId,
            @Valid @RequestBody AdventureLorebookEntryRequest request) {

        return flatMapWithAuthenticatedUser(authenticatedUser -> {

            CreateAdventureLorebookEntry command = new CreateAdventureLorebookEntry(
                    adventureId,
                    request.name(),
                    request.regex(),
                    request.description(),
                    request.playerId(),
                    authenticatedUser.getDiscordId());

            return commandRunner.run(command);
        });
    }

    @PutMapping("/{entryId}")
    @ResponseStatus(code = HttpStatus.OK)
    public Mono<AdventureLorebookEntryDetails> updateLorebookEntry(
            @PathVariable(required = true) UUID adventureId,
            @PathVariable(required = true) UUID entryId,
            @Valid @RequestBody AdventureLorebookEntryRequest request) {

        return flatMapWithAuthenticatedUser(authenticatedUser -> {

            UpdateAdventureLorebookEntry command = new UpdateAdventureLorebookEntry(
                    entryId,
                    adventureId,
                    request.name(),
                    request.regex(),
                    request.description(),
                    request.playerId(),
                    authenticatedUser.getDiscordId());

            return commandRunner.run(command);
        });
    }

    @DeleteMapping("/{entryId}")
    @ResponseStatus(code = HttpStatus.OK)
    public Mono<Void> deleteLorebookEntry(
            @PathVariable(required = true) UUID adventureId,
            @PathVariable(required = true) UUID entryId) {

        return flatMapWithAuthenticatedUser(authenticatedUser -> {

            DeleteAdventureLorebookEntry command = new DeleteAdventureLorebookEntry(
                    entryId,
                    adventureId,
                    authenticatedUser.getDiscordId());

            commandRunner.run(command);

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
}
