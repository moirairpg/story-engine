package me.moirai.storyengine.infrastructure.inbound.rest.controller;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

import java.util.UUID;
import java.util.stream.Collectors;

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
import me.moirai.storyengine.common.dto.PermissionDto;
import me.moirai.storyengine.core.port.inbound.world.CreateWorld;
import me.moirai.storyengine.core.port.inbound.world.DeleteWorld;
import me.moirai.storyengine.core.port.inbound.world.GetWorldById;
import me.moirai.storyengine.core.port.inbound.world.SearchWorlds;
import me.moirai.storyengine.core.port.inbound.world.UpdateWorld;
import me.moirai.storyengine.core.port.inbound.world.WorldDetails;
import me.moirai.storyengine.core.port.inbound.world.WorldSortField;
import me.moirai.storyengine.core.port.inbound.world.WorldSummary;
import me.moirai.storyengine.infrastructure.inbound.rest.request.CreateWorldRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.UpdateWorldRequest;

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

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PaginatedResult<WorldSummary> search(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "view", required = true) SearchView view,
            @RequestParam(name = "sorting_field", required = false) WorldSortField sortingField,
            @RequestParam(name = "direction", required = false) SortDirection direction,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size) {

        return queryRunner.run(new SearchWorlds(
                name,
                view,
                sortingField,
                direction,
                page,
                size,
                getAuthenticatedUser().id()));
    }

    @GetMapping("/{worldId}")
    @ResponseStatus(code = HttpStatus.OK)
    @Authorize(operation = AuthorizationOperation.VIEW_WORLD, fields = "#worldId")
    public WorldDetails getWorldById(@PathVariable(required = true) UUID worldId) {

        var query = new GetWorldById(worldId);
        return queryRunner.run(query);
    }

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public WorldDetails createWorld(@Valid @RequestBody CreateWorldRequest request) {

        var permissions = emptyIfNull(request.permissions()).stream()
                .map(p -> new PermissionDto(p.userId(), p.level()))
                .collect(Collectors.toSet());

        var command = new CreateWorld(
                request.name(),
                request.description(),
                request.adventureStart(),
                request.visibility(),
                emptyIfNull(request.lorebook()).stream()
                        .map(entry -> new CreateWorld.LorebookEntry(
                                entry.name(),
                                entry.description()))
                        .toList(),
                permissions);

        return commandRunner.run(command);
    }

    @PutMapping("/{worldId}")
    @ResponseStatus(code = HttpStatus.OK)
    @Authorize(operation = AuthorizationOperation.UPDATE_WORLD, fields = "#worldId")
    public WorldDetails updateWorld(@PathVariable(required = true) UUID worldId,
            @Valid @RequestBody UpdateWorldRequest request) {

        var updatePermissions = emptyIfNull(request.permissions()).stream()
                .map(p -> new PermissionDto(p.userId(), p.level()))
                .collect(Collectors.toSet());

        var command = new UpdateWorld(
                worldId,
                request.name(),
                request.description(),
                request.adventureStart(),
                request.visibility(),
                updatePermissions);

        return commandRunner.run(command);
    }

    @DeleteMapping("/{worldId}")
    @ResponseStatus(code = HttpStatus.OK)
    @Authorize(operation = AuthorizationOperation.DELETE_WORLD, fields = "#worldId")
    public void deleteWorld(@PathVariable(required = true) UUID worldId) {

        var command = new DeleteWorld(worldId);
        commandRunner.run(command);
    }
}
