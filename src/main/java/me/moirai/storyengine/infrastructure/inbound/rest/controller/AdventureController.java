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
import me.moirai.storyengine.common.dto.CursorResult;
import me.moirai.storyengine.common.dto.PaginatedResult;
import me.moirai.storyengine.common.dto.PermissionDto;
import me.moirai.storyengine.common.enums.SearchView;
import me.moirai.storyengine.common.enums.SortDirection;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.common.web.SecurityContextAware;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureCatchUp;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureDetails;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureLorebookEntryDetails;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureSortField;
import me.moirai.storyengine.core.port.inbound.adventure.CatchUpResult;
import me.moirai.storyengine.core.port.inbound.adventure.MessageSummary;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventureMessages;
import me.moirai.storyengine.core.port.inbound.message.DeleteMessage;
import me.moirai.storyengine.core.port.inbound.message.EditMessage;
import me.moirai.storyengine.core.port.inbound.message.Go;
import me.moirai.storyengine.core.port.inbound.message.MessageResult;
import me.moirai.storyengine.core.port.inbound.message.Retry;
import me.moirai.storyengine.core.port.inbound.message.RetryFromMessage;
import me.moirai.storyengine.core.port.inbound.message.Say;
import me.moirai.storyengine.core.port.inbound.message.StartAdventure;
import me.moirai.storyengine.infrastructure.inbound.rest.request.EditMessageRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.SayRequest;
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

        var lorebookEntries = emptyIfNull(request.lorebook()).stream()
                .map(e -> new AdventureLorebookEntryDetails(null, null, e.name(), e.description(), e.playerId(), false, null, null))
                .collect(Collectors.toSet());

        var command = new CreateAdventure(
                request.name(),
                request.description(),
                request.worldId(),
                request.narratorName(),
                request.narratorPersonality(),
                request.visibility(),
                request.moderation(),
                request.isMultiplayer(),
                request.adventureStart(),
                lorebookEntries,
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

        var lorebookEntriesToAdd = emptyIfNull(request.lorebookEntriesToAdd()).stream()
                .map(e -> new UpdateAdventure.LorebookEntryToAdd(e.name(), e.description(), e.playerId()))
                .toList();

        var lorebookEntriesToUpdate = emptyIfNull(request.lorebookEntriesToUpdate()).stream()
                .map(e -> new UpdateAdventure.LorebookEntryToUpdate(e.id(), e.name(), e.description(), e.playerId()))
                .toList();

        var command = new UpdateAdventure(
                adventureId,
                request.name(),
                request.description(),
                request.adventureStart(),
                request.narratorName(),
                request.narratorPersonality(),
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
                        request.contextAttributes().bumpFrequency()),
                lorebookEntriesToAdd,
                lorebookEntriesToUpdate,
                emptyIfNull(request.lorebookEntriesToDelete()).stream().toList());

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

    @GetMapping("/{adventureId}/messages")
    @ResponseStatus(code = HttpStatus.OK)
    @Authorize(operation = AuthorizationOperation.VIEW_ADVENTURE, fields = "#adventureId")
    public CursorResult<MessageSummary> getMessages(
            @PathVariable UUID adventureId,
            @RequestParam(required = false) UUID lastMessageId,
            @RequestParam int size) {

        return queryRunner.run(new SearchAdventureMessages(adventureId, lastMessageId, size));
    }

    @GetMapping("/{adventureId}/catchup")
    @ResponseStatus(code = HttpStatus.OK)
    @Authorize(operation = AuthorizationOperation.VIEW_ADVENTURE, fields = "#adventureId")
    public CatchUpResult getCatchUp(@PathVariable UUID adventureId) {

        return queryRunner.run(new AdventureCatchUp(adventureId));
    }

    @PostMapping("/{adventureId}/start")
    @ResponseStatus(code = HttpStatus.OK)
    @Authorize(operation = AuthorizationOperation.VIEW_ADVENTURE, fields = "#adventureId")
    public MessageResult start(@PathVariable UUID adventureId) {

        return commandRunner.run(new StartAdventure(adventureId));
    }

    @PostMapping("/{adventureId}/go")
    @ResponseStatus(code = HttpStatus.OK)
    @Authorize(operation = AuthorizationOperation.VIEW_ADVENTURE, fields = "#adventureId")
    public MessageResult go(@PathVariable UUID adventureId) {

        return commandRunner.run(new Go(adventureId));
    }

    @PostMapping("/{adventureId}/retry")
    @ResponseStatus(code = HttpStatus.OK)
    @Authorize(operation = AuthorizationOperation.VIEW_ADVENTURE, fields = "#adventureId")
    public MessageResult retry(@PathVariable UUID adventureId) {

        return commandRunner.run(new Retry(adventureId));
    }

    @PostMapping("/{adventureId}/say")
    @ResponseStatus(code = HttpStatus.OK)
    @Authorize(operation = AuthorizationOperation.VIEW_ADVENTURE, fields = "#adventureId")
    public MessageResult say(
            @PathVariable UUID adventureId,
            @RequestBody SayRequest request) {

        return commandRunner.run(new Say(adventureId, request.content()));
    }

    @DeleteMapping("/{adventureId}/message/{messageId}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    @Authorize(operation = AuthorizationOperation.UPDATE_ADVENTURE, fields = "#adventureId")
    public void deleteMessage(
            @PathVariable UUID adventureId,
            @PathVariable UUID messageId) {

        commandRunner.run(new DeleteMessage(adventureId, messageId));
    }

    @PostMapping("/{adventureId}/message/{messageId}/retry")
    @ResponseStatus(code = HttpStatus.OK)
    @Authorize(operation = AuthorizationOperation.UPDATE_ADVENTURE, fields = "#adventureId")
    public MessageResult retryFromMessage(
            @PathVariable UUID adventureId,
            @PathVariable UUID messageId) {

        return commandRunner.run(new RetryFromMessage(adventureId, messageId));
    }

    @PatchMapping("/{adventureId}/message/{messageId}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    @Authorize(operation = AuthorizationOperation.UPDATE_ADVENTURE, fields = "#adventureId")
    public void editMessage(
            @PathVariable UUID adventureId,
            @PathVariable UUID messageId,
            @Valid @RequestBody EditMessageRequest request) {

        commandRunner.run(new EditMessage(adventureId, messageId, request.content(), authenticatedUsername()));
    }

}
