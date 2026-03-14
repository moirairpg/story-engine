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
import me.moirai.storyengine.core.port.inbound.adventure.AddFavoriteAdventure;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureDetails;
import me.moirai.storyengine.core.port.inbound.adventure.CreateAdventure;
import me.moirai.storyengine.core.port.inbound.adventure.DeleteAdventure;
import me.moirai.storyengine.core.port.inbound.adventure.GetAdventureById;
import me.moirai.storyengine.core.port.inbound.adventure.RemoveFavoriteAdventure;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventures;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventuresResult;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventure;
import me.moirai.storyengine.infrastructure.inbound.rest.mapper.AdventureRequestMapper;
import me.moirai.storyengine.infrastructure.inbound.rest.request.AdventureSearchParameters;
import me.moirai.storyengine.infrastructure.inbound.rest.request.CreateAdventureRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.FavoriteRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.UpdateAdventureRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.enums.SearchDirection;
import me.moirai.storyengine.infrastructure.inbound.rest.request.enums.SearchGameMode;
import me.moirai.storyengine.infrastructure.inbound.rest.request.enums.SearchModel;
import me.moirai.storyengine.infrastructure.inbound.rest.request.enums.SearchModeration;
import me.moirai.storyengine.infrastructure.inbound.rest.request.enums.SearchOperation;
import me.moirai.storyengine.infrastructure.inbound.rest.request.enums.SearchSortingField;
import me.moirai.storyengine.infrastructure.inbound.rest.request.enums.SearchVisibility;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/adventure")
@Tag(name = "Adventures", description = "Endpoints for managing MoirAI Adventures")
public class AdventureController extends SecurityContextAware {

    private final UseCaseRunner useCaseRunner;
    private final AdventureRequestMapper requestMapper;

    public AdventureController(UseCaseRunner useCaseRunner,
            AdventureRequestMapper requestMapper) {

        this.useCaseRunner = useCaseRunner;
        this.requestMapper = requestMapper;
    }

    @GetMapping
    @ResponseStatus(code = HttpStatus.OK)
    public Mono<SearchAdventuresResult> search(AdventureSearchParameters searchParameters) {

        return mapWithAuthenticatedUser(authenticatedUser -> {

            SearchAdventures query = SearchAdventures.builder()
                    .name(searchParameters.getName())
                    .world(searchParameters.getWorld())
                    .persona(searchParameters.getPersona())
                    .ownerId(searchParameters.getOwnerId())
                    .favorites(searchParameters.isFavorites())
                    .multiplayer(searchParameters.isMultiplayer())
                    .page(searchParameters.getPage())
                    .size(searchParameters.getSize())
                    .model(getModel(searchParameters.getModel()))
                    .gameMode(getGameMode(searchParameters.getGameMode()))
                    .moderation(getModeration(searchParameters.getModeration()))
                    .sortingField(getSortingField(searchParameters.getSortingField()))
                    .direction(getDirection(searchParameters.getDirection()))
                    .visibility(getVisibility(searchParameters.getVisibility()))
                    .operation(getOperation(searchParameters.getOperation()))
                    .requesterId(authenticatedUser.getDiscordId())
                    .build();

            return useCaseRunner.run(query);
        });
    }

    @GetMapping("/{adventureId}")
    @ResponseStatus(code = HttpStatus.OK)
    @PreAuthorize("canRead(#adventureId, 'Adventure')")
    public Mono<AdventureDetails> getAdventureById(
            @PathVariable(required = true) String adventureId) {

        return mapWithAuthenticatedUser(authenticatedUser -> {

            GetAdventureById query = GetAdventureById.build(adventureId, authenticatedUser.getDiscordId());
            return useCaseRunner.run(query);
        });
    }

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    @PreAuthorize("canRead(#request.personaId, 'Persona') && canRead(#request.worldId, 'World')")
    public Mono<AdventureDetails> createAdventure(
            @Valid @RequestBody CreateAdventureRequest request) {

        return mapWithAuthenticatedUser(authenticatedUser -> {

            CreateAdventure command = requestMapper.toCommand(request, authenticatedUser.getDiscordId());
            return useCaseRunner.run(command);
        });
    }

    @PutMapping("/{adventureId}")
    @ResponseStatus(code = HttpStatus.OK)
    @PreAuthorize("canModify(#adventureId, 'Adventure') && canRead(#request.personaId, 'Persona') && canRead(#request.worldId, 'World')")
    public Mono<AdventureDetails> updateAdventure(
            @PathVariable(required = true) String adventureId,
            @Valid @RequestBody UpdateAdventureRequest request) {

        return mapWithAuthenticatedUser(authenticatedUser -> {

            UpdateAdventure command = requestMapper.toCommand(request, adventureId, authenticatedUser.getDiscordId());
            return useCaseRunner.run(command);
        });
    }

    @DeleteMapping("/{adventureId}")
    @ResponseStatus(code = HttpStatus.OK)
    @PreAuthorize("canModify(#adventureId, 'Adventure')")
    public Mono<Void> deleteAdventure(
            @PathVariable(required = true) String adventureId) {

        return flatMapWithAuthenticatedUser(authenticatedUser -> {

            DeleteAdventure command = requestMapper.toCommand(adventureId, authenticatedUser.getDiscordId());
            useCaseRunner.run(command);

            return Mono.empty();
        });
    }

    @PostMapping("/favorite")
    @ResponseStatus(code = HttpStatus.CREATED)
    @PreAuthorize("canRead(#request.assetId, 'Adventure')")
    public Mono<Void> addFavoriteAdventure(@RequestBody FavoriteRequest request) {

        return flatMapWithAuthenticatedUser(authenticatedUser -> {

            AddFavoriteAdventure command = AddFavoriteAdventure.builder()
                    .assetId(request.getAssetId())
                    .playerId(authenticatedUser.getDiscordId())
                    .build();

            useCaseRunner.run(command);

            return Mono.empty();
        });
    }

    @DeleteMapping("/favorite/{assetId}")
    @ResponseStatus(code = HttpStatus.CREATED)
    public Mono<Void> removeFavoriteAdventure(@PathVariable(required = true) String assetId) {

        return flatMapWithAuthenticatedUser(authenticatedUser -> {

            RemoveFavoriteAdventure command = RemoveFavoriteAdventure.builder()
                    .assetId(assetId)
                    .playerId(authenticatedUser.getDiscordId())
                    .build();

            useCaseRunner.run(command);

            return Mono.empty();
        });
    }

    private String getModel(SearchModel searchModel) {

        if (searchModel != null) {
            return searchModel.name();
        }

        return EMPTY;
    }

    private String getGameMode(SearchGameMode searchGameMode) {

        if (searchGameMode != null) {
            return searchGameMode.name();
        }

        return EMPTY;
    }

    private String getModeration(SearchModeration searchModeration) {

        if (searchModeration != null) {
            return searchModeration.name();
        }

        return EMPTY;
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
