package me.moirai.storyengine.infrastructure.inbound.rest.controller;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;
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
import me.moirai.storyengine.common.annotation.Authorize;
import me.moirai.storyengine.common.cqs.command.CommandRunner;
import me.moirai.storyengine.common.cqs.query.QueryRunner;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.common.web.SecurityContextAware;
import me.moirai.storyengine.core.port.inbound.world.CreateWorld;
import me.moirai.storyengine.core.port.inbound.world.DeleteWorld;
import me.moirai.storyengine.core.port.inbound.world.GetWorldById;
import me.moirai.storyengine.core.port.inbound.world.SearchWorlds;
import me.moirai.storyengine.core.port.inbound.world.SearchWorldsResult;
import me.moirai.storyengine.core.port.inbound.world.UpdateWorld;
import me.moirai.storyengine.core.port.inbound.world.WorldDetails;
import me.moirai.storyengine.infrastructure.inbound.rest.request.CreateWorldRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.UpdateWorldRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.WorldSearchParameters;
import me.moirai.storyengine.infrastructure.inbound.rest.request.enums.SearchDirection;
import me.moirai.storyengine.infrastructure.inbound.rest.request.enums.SearchOperation;
import me.moirai.storyengine.infrastructure.inbound.rest.request.enums.SearchSortingField;
import me.moirai.storyengine.infrastructure.inbound.rest.request.enums.SearchVisibility;

@RestController
@RequestMapping("/world")
@Tag(name = "Worlds", description = "Endpoints for managing MoirAI Worlds")
public class WorldController extends SecurityContextAware {

    private final QueryRunner queryRunner;
    private final CommandRunner commandRunner;

    public WorldController(
            QueryRunner queryRunner,
            CommandRunner commandRunner) {

        this.queryRunner = queryRunner;
        this.commandRunner = commandRunner;
    }

    // TODO reform search request
    @GetMapping
    @ResponseStatus(code = HttpStatus.OK)
    public SearchWorldsResult search(WorldSearchParameters searchParameters) {

        var query = new SearchWorlds(
                searchParameters.getName(),
                searchParameters.getOwnerId(),
                searchParameters.getPage(),
                searchParameters.getSize(),
                getSortingField(searchParameters.getSortingField()),
                getDirection(searchParameters.getDirection()),
                getVisibility(searchParameters.getVisibility()),
                getOperation(searchParameters.getOperation()),
                authenticatedUserId());

        return queryRunner.run(query);
    }

    @GetMapping("/{worldId}")
    @ResponseStatus(code = HttpStatus.OK)
    @Authorize(operation = AuthorizationOperation.VIEW_WORLD, fields = "#worldId")
    public WorldDetails getWorldById(@PathVariable(required = true) UUID worldId) {

        var query = new GetWorldById(worldId, authenticatedUserId());
        return queryRunner.run(query);
    }

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public WorldDetails createWorld(@Valid @RequestBody CreateWorldRequest request) {

        var command = new CreateWorld(
                request.name(),
                request.description(),
                request.adventureStart(),
                request.visibility(),
                emptyIfNull(request.lorebook()).stream()
                        .map(entry -> new CreateWorld.LorebookEntry(
                                entry.name(),
                                entry.regex(),
                                entry.description()))
                        .toList(),
                request.usersAllowedToWrite(),
                request.usersAllowedToRead(),
                authenticatedUserId());

        return commandRunner.run(command);
    }

    @PutMapping("/{worldId}")
    @ResponseStatus(code = HttpStatus.OK)
    @Authorize(operation = AuthorizationOperation.UPDATE_WORLD, fields = "#worldId")
    public WorldDetails updateWorld(@PathVariable(required = true) UUID worldId,
            @Valid @RequestBody UpdateWorldRequest request) {

        var command = new UpdateWorld(
                worldId,
                request.name(),
                request.description(),
                request.adventureStart(),
                request.visibility(),
                authenticatedUserId(),
                request.usersAllowedToWriteToAdd(),
                request.usersAllowedToWriteToRemove(),
                request.usersAllowedToReadToAdd(),
                request.usersAllowedToReadToRemove());

        return commandRunner.run(command);
    }

    @DeleteMapping("/{worldId}")
    @ResponseStatus(code = HttpStatus.OK)
    @Authorize(operation = AuthorizationOperation.DELETE_WORLD, fields = "#worldId")
    public void deleteWorld(@PathVariable(required = true) UUID worldId) {

        var command = new DeleteWorld(worldId, authenticatedUserId());
        commandRunner.run(command);
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
