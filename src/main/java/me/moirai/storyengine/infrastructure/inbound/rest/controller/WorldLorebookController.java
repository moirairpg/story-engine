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
import me.moirai.storyengine.core.application.usecase.world.request.CreateWorldLorebookEntry;
import me.moirai.storyengine.core.application.usecase.world.request.DeleteWorldLorebookEntry;
import me.moirai.storyengine.core.application.usecase.world.request.GetWorldLorebookEntryById;
import me.moirai.storyengine.core.application.usecase.world.request.SearchWorldLorebookEntries;
import me.moirai.storyengine.core.application.usecase.world.request.UpdateWorldLorebookEntry;
import me.moirai.storyengine.infrastructure.inbound.rest.mapper.WorldLorebookEntryRequestMapper;
import me.moirai.storyengine.infrastructure.inbound.rest.mapper.WorldLorebookEntryResponseMapper;
import me.moirai.storyengine.infrastructure.inbound.rest.request.CreateLorebookEntryRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.LorebookSearchParameters;
import me.moirai.storyengine.infrastructure.inbound.rest.request.UpdateLorebookEntryRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.enums.SearchDirection;
import me.moirai.storyengine.infrastructure.inbound.rest.request.enums.SearchSortingField;
import me.moirai.storyengine.infrastructure.inbound.rest.response.CreateLorebookEntryResponse;
import me.moirai.storyengine.infrastructure.inbound.rest.response.LorebookEntryResponse;
import me.moirai.storyengine.infrastructure.inbound.rest.response.SearchLorebookEntriesResponse;
import me.moirai.storyengine.infrastructure.inbound.rest.response.UpdateLorebookEntryResponse;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/world/{worldId}/lorebook")
@Tag(name = "World Lorebooks", description = "Endpoints for managing MoirAI World Lorebooks")
public class WorldLorebookController extends SecurityContextAware {

    private final UseCaseRunner useCaseRunner;
    private final WorldLorebookEntryRequestMapper requestMapper;
    private final WorldLorebookEntryResponseMapper responseMapper;

    public WorldLorebookController(UseCaseRunner useCaseRunner,
            WorldLorebookEntryRequestMapper requestMapper,
            WorldLorebookEntryResponseMapper responseMapper) {

        this.useCaseRunner = useCaseRunner;
        this.requestMapper = requestMapper;
        this.responseMapper = responseMapper;
    }

    @GetMapping
    @ResponseStatus(code = HttpStatus.OK)
    @PreAuthorize("canRead(#worldId, 'World')")
    public Mono<SearchLorebookEntriesResponse> search(
            @PathVariable(required = true) String worldId,
            LorebookSearchParameters searchParameters) {

        return mapWithAuthenticatedUser(authenticatedUser -> {

            SearchWorldLorebookEntries query = SearchWorldLorebookEntries.builder()
                    .page(searchParameters.getPage())
                    .size(searchParameters.getSize())
                    .sortingField(getSortingField(searchParameters.getSortingField()))
                    .direction(getDirection(searchParameters.getDirection()))
                    .name(searchParameters.getName())
                    .requesterId(authenticatedUser.getDiscordId())
                    .worldId(worldId)
                    .build();

            return responseMapper.toResponse(useCaseRunner.run(query));
        });
    }

    @GetMapping("/{entryId}")
    @ResponseStatus(code = HttpStatus.OK)
    @PreAuthorize("canRead(#worldId, 'World')")
    public Mono<LorebookEntryResponse> getLorebookEntryById(
            @PathVariable(required = true) String worldId,
            @PathVariable(required = true) String entryId) {

        return mapWithAuthenticatedUser(authenticatedUser -> {

            GetWorldLorebookEntryById query = GetWorldLorebookEntryById.builder()
                    .entryId(entryId)
                    .worldId(worldId)
                    .requesterId(authenticatedUser.getDiscordId())
                    .build();

            return responseMapper.toResponse(useCaseRunner.run(query));
        });
    }

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    @PreAuthorize("canModify(#worldId, 'World')")
    public Mono<CreateLorebookEntryResponse> createLorebookEntry(
            @PathVariable(required = true) String worldId,
            @Valid @RequestBody CreateLorebookEntryRequest request) {

        return mapWithAuthenticatedUser(authenticatedUser -> {

            CreateWorldLorebookEntry command = requestMapper.toCommand(request, worldId, authenticatedUser.getDiscordId());
            return responseMapper.toResponse(useCaseRunner.run(command));
        });
    }

    @PutMapping("/{entryId}")
    @ResponseStatus(code = HttpStatus.OK)
    @PreAuthorize("canModify(#worldId, 'World')")
    public Mono<UpdateLorebookEntryResponse> updateLorebookEntry(
            @PathVariable(required = true) String worldId,
            @PathVariable(required = true) String entryId,
            @Valid @RequestBody UpdateLorebookEntryRequest request) {

        return mapWithAuthenticatedUser(authenticatedUser -> {

            UpdateWorldLorebookEntry command = requestMapper.toCommand(request, entryId,
                    worldId, authenticatedUser.getDiscordId());

            return responseMapper.toResponse(useCaseRunner.run(command));
        });
    }

    @DeleteMapping("/{entryId}")
    @ResponseStatus(code = HttpStatus.OK)
    @PreAuthorize("canModify(#worldId, 'World')")
    public Mono<Void> deleteLorebookEntry(
            @PathVariable(required = true) String worldId,
            @PathVariable(required = true) String entryId) {

        return flatMapWithAuthenticatedUser(authenticatedUser -> {

            DeleteWorldLorebookEntry command = requestMapper.toCommand(entryId, worldId, authenticatedUser.getDiscordId());
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
}
