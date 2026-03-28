package me.moirai.storyengine.infrastructure.inbound.rest.controller;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
import me.moirai.storyengine.common.dto.PermissionDto;
import me.moirai.storyengine.common.enums.SearchView;
import me.moirai.storyengine.common.enums.SortDirection;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.common.web.SecurityContextAware;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureDetails;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureSortField;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureSummary;
import me.moirai.storyengine.core.port.inbound.adventure.ContextAttributesDto;
import me.moirai.storyengine.core.port.inbound.adventure.CreateAdventure;
import me.moirai.storyengine.core.port.inbound.adventure.DeleteAdventure;
import me.moirai.storyengine.core.port.inbound.adventure.GetAdventureById;
import me.moirai.storyengine.core.port.inbound.adventure.ModelConfigurationDto;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventures;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventure;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventureAuthorsNoteById;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventureBumpById;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventureNudgeById;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventureSceneById;
import me.moirai.storyengine.infrastructure.inbound.rest.request.CreateAdventureRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.UpdateAdventureAuthorsNoteRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.UpdateAdventureBumpRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.UpdateAdventureNudgeRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.UpdateAdventureRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.UpdateAdventureSceneRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.enums.SearchGameMode;
import me.moirai.storyengine.infrastructure.inbound.rest.request.enums.SearchModel;
import me.moirai.storyengine.infrastructure.inbound.rest.request.enums.SearchModeration;

@RestController
@RequestMapping("/adventure")
@Tag(name = "Adventures", description = "Endpoints for managing MoirAI Adventures")
public class AdventureController extends SecurityContextAware {

    private final QueryRunner queryRunner;
    private final CommandRunner commandRunner;

    public AdventureController(
            QueryRunner queryRunner,
            CommandRunner commandRunner) {

        this.queryRunner = queryRunner;
        this.commandRunner = commandRunner;
    }

    @GetMapping
    @ResponseStatus(code = HttpStatus.OK)
    public PaginatedResult<AdventureSummary> search(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "world_name", required = false) String worldName,
            @RequestParam(name = "persona_name", required = false) String personaName,
            @RequestParam(name = "is_multiplayer", required = false) Boolean isMultiplayer,
            @RequestParam(name = "model", required = false) SearchModel model,
            @RequestParam(name = "game_mode", required = false) SearchGameMode gameMode,
            @RequestParam(name = "moderation", required = false) SearchModeration moderation,
            @RequestParam(name = "view", required = true) SearchView view,
            @RequestParam(name = "sorting_field", required = false) AdventureSortField sortingField,
            @RequestParam(name = "direction", required = false) SortDirection direction,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size) {

        return queryRunner.run(new SearchAdventures(
                name,
                worldName,
                personaName,
                isMultiplayer,
                model != null ? model.name() : null,
                gameMode != null ? gameMode.name() : null,
                moderation != null ? moderation.name() : null,
                view,
                sortingField,
                direction,
                page,
                size,
                getAuthenticatedUser().id()));
    }

    @GetMapping("/{adventureId}")
    @ResponseStatus(code = HttpStatus.OK)
    @Authorize(operation = AuthorizationOperation.VIEW_ADVENTURE, fields = "#adventureId")
    public AdventureDetails getAdventureById(
            @PathVariable(required = true) UUID adventureId) {

        var query = new GetAdventureById(adventureId);
        return queryRunner.run(query);
    }

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public AdventureDetails createAdventure(
            @Valid @RequestBody CreateAdventureRequest request) {

        var permissions = emptyIfNull(request.permissions()).stream()
                .map(p -> new PermissionDto(p.userId(), p.level()))
                .collect(Collectors.toSet());

        var command = new CreateAdventure(
                request.name(),
                request.description(),
                request.worldId(),
                request.personaId(),
                request.visibility(),
                request.moderation(),
                request.isMultiplayer(),
                permissions,
                new ModelConfigurationDto(
                        request.modelConfiguration().aiModel(),
                        request.modelConfiguration().maxTokenLimit(),
                        request.modelConfiguration().temperature()),
                new ContextAttributesDto(
                        request.contextAttributes().nudge(),
                        request.contextAttributes().authorsNote(),
                        request.contextAttributes().scene(),
                        request.contextAttributes().bump(),
                        request.contextAttributes().bumpFrequency()));

        return commandRunner.run(command);
    }

    @PutMapping("/{adventureId}")
    @ResponseStatus(code = HttpStatus.OK)
    @Authorize(operation = AuthorizationOperation.UPDATE_ADVENTURE, fields = "#adventureId")
    public AdventureDetails updateAdventure(
            @PathVariable(required = true) UUID adventureId,
            @Valid @RequestBody UpdateAdventureRequest request) {

        var updatePermissions = emptyIfNull(request.permissions()).stream()
                .map(p -> new PermissionDto(p.userId(), p.level()))
                .collect(Collectors.toSet());

        var command = new UpdateAdventure(
                adventureId,
                request.description(),
                request.adventureStart(),
                request.name(),
                request.worldId(),
                request.personaId(),
                request.visibility(),
                request.moderation(),
                request.isMultiplayer(),
                updatePermissions,
                new ModelConfigurationDto(
                        request.modelConfiguration().aiModel(),
                        request.modelConfiguration().maxTokenLimit(),
                        request.modelConfiguration().temperature()),
                new ContextAttributesDto(
                        request.contextAttributes().nudge(),
                        request.contextAttributes().authorsNote(),
                        request.contextAttributes().scene(),
                        request.contextAttributes().bump(),
                        request.contextAttributes().bumpFrequency()));

        return commandRunner.run(command);
    }

    @PatchMapping("/{adventureId}/authors-note")
    @ResponseStatus(code = HttpStatus.OK)
    @Authorize(operation = AuthorizationOperation.UPDATE_ADVENTURE, fields = "#adventureId")
    public void updateAuthorsNote(
            @PathVariable(required = true) UUID adventureId,
            @Valid @RequestBody UpdateAdventureAuthorsNoteRequest request) {

        commandRunner.run(new UpdateAdventureAuthorsNoteById(request.authorsNote(), adventureId));
    }

    @PatchMapping("/{adventureId}/bump")
    @ResponseStatus(code = HttpStatus.OK)
    @Authorize(operation = AuthorizationOperation.UPDATE_ADVENTURE, fields = "#adventureId")
    public void updateBump(
            @PathVariable(required = true) UUID adventureId,
            @Valid @RequestBody UpdateAdventureBumpRequest request) {

        commandRunner.run(new UpdateAdventureBumpById(request.bump(), request.bumpFrequency(), adventureId));
    }

    @PatchMapping("/{adventureId}/nudge")
    @ResponseStatus(code = HttpStatus.OK)
    @Authorize(operation = AuthorizationOperation.UPDATE_ADVENTURE, fields = "#adventureId")
    public void updateNudge(
            @PathVariable(required = true) UUID adventureId,
            @Valid @RequestBody UpdateAdventureNudgeRequest request) {

        commandRunner.run(new UpdateAdventureNudgeById(request.nudge(), adventureId));
    }

    @PatchMapping("/{adventureId}/scene")
    @ResponseStatus(code = HttpStatus.OK)
    @Authorize(operation = AuthorizationOperation.UPDATE_ADVENTURE, fields = "#adventureId")
    public void updateScene(
            @PathVariable(required = true) UUID adventureId,
            @Valid @RequestBody UpdateAdventureSceneRequest request) {

        commandRunner.run(new UpdateAdventureSceneById(request.scene(), adventureId));
    }

    @DeleteMapping("/{adventureId}")
    @ResponseStatus(code = HttpStatus.OK)
    @Authorize(operation = AuthorizationOperation.DELETE_ADVENTURE, fields = "#adventureId")
    public void deleteAdventure(
            @PathVariable(required = true) UUID adventureId) {

        var command = new DeleteAdventure(adventureId);
        commandRunner.run(command);
    }

}
