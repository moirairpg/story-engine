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
import me.moirai.storyengine.core.port.inbound.world.AddFavoriteWorld;
import me.moirai.storyengine.core.port.inbound.world.CreateWorld;
import me.moirai.storyengine.core.port.inbound.world.DeleteWorld;
import me.moirai.storyengine.core.port.inbound.world.GetWorldById;
import me.moirai.storyengine.core.port.inbound.world.RemoveFavoriteWorld;
import me.moirai.storyengine.core.port.inbound.world.SearchWorlds;
import me.moirai.storyengine.core.port.inbound.world.UpdateWorld;
import me.moirai.storyengine.infrastructure.inbound.rest.mapper.WorldRequestMapper;
import me.moirai.storyengine.infrastructure.inbound.rest.mapper.WorldResponseMapper;
import me.moirai.storyengine.infrastructure.inbound.rest.request.CreateWorldRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.FavoriteRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.UpdateWorldRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.WorldSearchParameters;
import me.moirai.storyengine.infrastructure.inbound.rest.request.enums.SearchDirection;
import me.moirai.storyengine.infrastructure.inbound.rest.request.enums.SearchOperation;
import me.moirai.storyengine.infrastructure.inbound.rest.request.enums.SearchSortingField;
import me.moirai.storyengine.infrastructure.inbound.rest.request.enums.SearchVisibility;
import me.moirai.storyengine.infrastructure.inbound.rest.response.CreateWorldResponse;
import me.moirai.storyengine.infrastructure.inbound.rest.response.SearchWorldsResponse;
import me.moirai.storyengine.infrastructure.inbound.rest.response.UpdateWorldResponse;
import me.moirai.storyengine.infrastructure.inbound.rest.response.WorldResponse;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/world")
@Tag(name = "Worlds", description = "Endpoints for managing MoirAI Worlds")
public class WorldController extends SecurityContextAware {

    private final UseCaseRunner useCaseRunner;
    private final WorldResponseMapper responseMapper;
    private final WorldRequestMapper requestMapper;

    public WorldController(UseCaseRunner useCaseRunner,
            WorldResponseMapper responseMapper,
            WorldRequestMapper requestMapper) {

        this.useCaseRunner = useCaseRunner;
        this.responseMapper = responseMapper;
        this.requestMapper = requestMapper;
    }

    @GetMapping
    @ResponseStatus(code = HttpStatus.OK)
    public Mono<SearchWorldsResponse> search(WorldSearchParameters searchParameters) {

        return mapWithAuthenticatedUser(authenticatedUser -> {

            SearchWorlds query = SearchWorlds.builder()
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

            return responseMapper.toResponse(useCaseRunner.run(query));
        });
    }

    @GetMapping("/{worldId}")
    @ResponseStatus(code = HttpStatus.OK)
    @PreAuthorize("canRead(#worldId, 'World')")
    public Mono<WorldResponse> getWorldById(@PathVariable(required = true) String worldId) {

        return mapWithAuthenticatedUser(authenticatedUser -> {

            GetWorldById query = GetWorldById.build(worldId, authenticatedUser.getDiscordId());
            return responseMapper.toResponse(useCaseRunner.run(query));
        });
    }

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public Mono<CreateWorldResponse> createWorld(@Valid @RequestBody CreateWorldRequest request) {

        return flatMapWithAuthenticatedUser(authenticatedUser -> {

            CreateWorld command = requestMapper.toCommand(request, authenticatedUser.getDiscordId());
            return useCaseRunner.run(command);
        }).map(responseMapper::toResponse);
    }

    @PutMapping("/{worldId}")
    @ResponseStatus(code = HttpStatus.OK)
    @PreAuthorize("canModify(#worldId, 'World')")
    public Mono<UpdateWorldResponse> updateWorld(@PathVariable(required = true) String worldId,
            @Valid @RequestBody UpdateWorldRequest request) {

        return flatMapWithAuthenticatedUser(authenticatedUser -> {

            UpdateWorld command = requestMapper.toCommand(request, worldId, authenticatedUser.getDiscordId());
            return useCaseRunner.run(command);
        }).map(responseMapper::toResponse);
    }

    @DeleteMapping("/{worldId}")
    @ResponseStatus(code = HttpStatus.OK)
    @PreAuthorize("canModify(#worldId, 'World')")
    public Mono<Void> deleteWorld(@PathVariable(required = true) String worldId) {

        return flatMapWithAuthenticatedUser(authenticatedUser -> {

            DeleteWorld command = DeleteWorld.build(worldId, authenticatedUser.getDiscordId());
            useCaseRunner.run(command);

            return Mono.empty();
        });
    }

    @PostMapping("/favorite")
    @ResponseStatus(code = HttpStatus.CREATED)
    @PreAuthorize("canRead(#request.assetId, 'World')")
    public Mono<Void> addFavoriteWorld(@RequestBody FavoriteRequest request) {

        return flatMapWithAuthenticatedUser(authenticatedUser -> {

            AddFavoriteWorld command = AddFavoriteWorld.builder()
                    .assetId(request.getAssetId())
                    .playerId(authenticatedUser.getDiscordId())
                    .build();

            useCaseRunner.run(command);

            return Mono.empty();
        });
    }

    @DeleteMapping("/favorite/{assetId}")
    @ResponseStatus(code = HttpStatus.CREATED)
    public Mono<Void> removeFavoriteWorld(@PathVariable(required = true) String assetId) {

        return flatMapWithAuthenticatedUser(authenticatedUser -> {

            RemoveFavoriteWorld command = RemoveFavoriteWorld.builder()
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
