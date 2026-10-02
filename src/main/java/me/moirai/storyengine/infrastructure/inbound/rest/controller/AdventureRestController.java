package me.moirai.storyengine.infrastructure.inbound.rest.controller;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
import me.moirai.storyengine.common.cqs.command.CommandRunner;
import me.moirai.storyengine.common.cqs.query.QueryRunner;
import me.moirai.storyengine.common.dto.CursorResult;
import me.moirai.storyengine.common.dto.MessageSummary;
import me.moirai.storyengine.common.dto.PaginatedResult;
import me.moirai.storyengine.common.dto.PermissionDto;
import me.moirai.storyengine.core.port.inbound.AssetMember;
import me.moirai.storyengine.core.port.inbound.AssetMemberInput;
import me.moirai.storyengine.core.port.inbound.ImageResult;
import me.moirai.storyengine.core.port.inbound.adventure.GetAdventureMembers;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventurePermissions;
import me.moirai.storyengine.infrastructure.inbound.rest.request.UpdateAssetPermissionsRequest;
import me.moirai.storyengine.core.port.inbound.adventure.DeclineAdventureInvitation;
import me.moirai.storyengine.core.port.inbound.adventure.GetPendingAdventureInvitation;
import me.moirai.storyengine.core.port.inbound.adventure.InviteUserToAdventure;
import me.moirai.storyengine.core.port.inbound.adventure.InviteUserToAdventureResult;
import me.moirai.storyengine.core.port.inbound.adventure.JoinAdventureWithCharacter;
import me.moirai.storyengine.core.port.inbound.adventure.RemoveCharacterFromAdventure;
import me.moirai.storyengine.core.port.inbound.adventure.PendingAdventureInvitationDetails;
import me.moirai.storyengine.core.port.inbound.adventure.RemoveAdventureImage;
import me.moirai.storyengine.core.port.inbound.adventure.UploadAdventureImage;
import me.moirai.storyengine.infrastructure.inbound.rest.request.InviteUserToAdventureRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.JoinAdventureWithCharacterRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.UploadImageRequest;
import me.moirai.storyengine.common.enums.SearchView;
import me.moirai.storyengine.common.enums.SortDirection;
import me.moirai.storyengine.common.util.Functions;
import me.moirai.storyengine.common.web.SecurityContextAware;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureCatchUp;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureDetails;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureLorebookEntryDetails;
import me.moirai.storyengine.core.port.inbound.adventure.AdventureSortField;
import me.moirai.storyengine.core.port.inbound.adventure.CatchUpResult;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventureMessages;
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
import me.moirai.storyengine.infrastructure.inbound.rest.request.enums.SearchModel;
import me.moirai.storyengine.infrastructure.inbound.rest.request.enums.SearchModeration;

@RestController
@RequestMapping("/adventures")
@Tag(name = "Adventures", description = "Endpoints for managing MoirAI Adventures")
public class AdventureRestController extends SecurityContextAware {

    private final QueryRunner queryRunner;
    private final CommandRunner commandRunner;

    public AdventureRestController(
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
            @RequestParam(name = "model", required = false) SearchModel model,
            @RequestParam(name = "moderation", required = false) SearchModeration moderation,
            @RequestParam(name = "view", required = true) SearchView view,
            @RequestParam(name = "sorting_field", required = false) AdventureSortField sortingField,
            @RequestParam(name = "direction", required = false) SortDirection direction,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size) {

        return queryRunner.run(new SearchAdventures(
                name,
                worldName,
                Functions.mapOrNull(model, SearchModel::name),
                Functions.mapOrNull(moderation, SearchModeration::name),
                view,
                sortingField,
                direction,
                page,
                size,
                getAuthenticatedUser().id()));
    }

    @GetMapping("/{adventureId}")
    @ResponseStatus(code = HttpStatus.OK)
    public AdventureDetails getAdventureById(
            @PathVariable(required = true) UUID adventureId) {

        var query = new GetAdventureById(adventureId, authenticatedUserId());
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
                .map(e -> new AdventureLorebookEntryDetails(
                        null,
                        null,
                        e.name(),
                        e.description(),
                        null,
                        null))
                .collect(Collectors.toSet());

        var command = new CreateAdventure(
                request.name(),
                request.description(),
                request.worldId(),
                request.narratorName(),
                request.narratorPersonality(),
                request.visibility(),
                request.moderation(),
                request.rpgMechanicsEnabled(),
                request.adventureStart(),
                lorebookEntries,
                request.uiImagePositionX(),
                request.uiImagePositionY(),
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
    public AdventureDetails updateAdventure(
            @PathVariable(required = true) UUID adventureId,
            @Valid @RequestBody UpdateAdventureRequest request) {

        var lorebookEntriesToAdd = emptyIfNull(request.lorebookEntriesToAdd()).stream()
                .map(e -> new UpdateAdventure.LorebookEntryToAdd(e.name(), e.description()))
                .toList();

        var lorebookEntriesToUpdate = emptyIfNull(request.lorebookEntriesToUpdate()).stream()
                .map(e -> new UpdateAdventure.LorebookEntryToUpdate(e.id(), e.name(), e.description()))
                .toList();

        var command = new UpdateAdventure(
                adventureId,
                request.name(),
                request.description(),
                request.adventureStart(),
                request.narratorName(),
                request.narratorPersonality(),
                request.moderation(),
                request.rpgMechanicsEnabled(),
                request.uiImagePositionX(),
                request.uiImagePositionY(),
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
                emptyIfNull(request.lorebookEntriesToDelete()).stream().toList(),
                authenticatedUserId());

        return commandRunner.run(command);
    }

    @PatchMapping("/{adventureId}/authors-note")
    @ResponseStatus(code = HttpStatus.OK)
    public void updateAuthorsNote(
            @PathVariable(required = true) UUID adventureId,
            @Valid @RequestBody UpdateAdventureAuthorsNoteRequest request) {

        commandRunner.run(new UpdateAdventureAuthorsNoteById(request.authorsNote(), adventureId));
    }

    @PatchMapping("/{adventureId}/bump")
    @ResponseStatus(code = HttpStatus.OK)
    public void updateBump(
            @PathVariable(required = true) UUID adventureId,
            @Valid @RequestBody UpdateAdventureBumpRequest request) {

        commandRunner.run(new UpdateAdventureBumpById(request.bump(), request.bumpFrequency(), adventureId));
    }

    @PatchMapping("/{adventureId}/nudge")
    @ResponseStatus(code = HttpStatus.OK)
    public void updateNudge(
            @PathVariable(required = true) UUID adventureId,
            @Valid @RequestBody UpdateAdventureNudgeRequest request) {

        commandRunner.run(new UpdateAdventureNudgeById(request.nudge(), adventureId));
    }

    @PatchMapping("/{adventureId}/scene")
    @ResponseStatus(code = HttpStatus.OK)
    public void updateScene(
            @PathVariable(required = true) UUID adventureId,
            @Valid @RequestBody UpdateAdventureSceneRequest request) {

        commandRunner.run(new UpdateAdventureSceneById(request.scene(), adventureId));
    }

    @DeleteMapping("/{adventureId}")
    @ResponseStatus(code = HttpStatus.OK)
    public void deleteAdventure(
            @PathVariable(required = true) UUID adventureId) {

        var command = new DeleteAdventure(adventureId);
        commandRunner.run(command);
    }

    @GetMapping("/{adventureId}/permissions")
    @ResponseStatus(code = HttpStatus.OK)
    public List<AssetMember> getAdventureMembers(@PathVariable(required = true) UUID adventureId) {

        return queryRunner.run(new GetAdventureMembers(adventureId));
    }

    @PutMapping("/{adventureId}/permissions")
    @ResponseStatus(code = HttpStatus.OK)
    public List<AssetMember> updateAdventurePermissions(
            @PathVariable(required = true) UUID adventureId,
            @Valid @RequestBody UpdateAssetPermissionsRequest request) {

        var members = request.members().stream()
                .map(member -> new AssetMemberInput(member.username(), member.level()))
                .toList();

        return commandRunner.run(new UpdateAdventurePermissions(adventureId, request.visibility(), members));
    }

    @GetMapping("/{adventureId}/messages")
    @ResponseStatus(code = HttpStatus.OK)
    public CursorResult<MessageSummary> getMessages(
            @PathVariable UUID adventureId,
            @RequestParam(required = false) UUID lastMessageId,
            @RequestParam int size) {

        return queryRunner.run(new SearchAdventureMessages(adventureId, lastMessageId, size));
    }

    @GetMapping("/{adventureId}/catchup")
    @ResponseStatus(code = HttpStatus.OK)
    public CatchUpResult getCatchUp(@PathVariable UUID adventureId) {

        return queryRunner.run(new AdventureCatchUp(adventureId));
    }

    @PutMapping(value = "/{adventureId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ImageResult uploadAdventureImage(
            @PathVariable UUID adventureId,
            @Valid @ModelAttribute UploadImageRequest request) throws IOException {

        var command = new UploadAdventureImage(
                adventureId,
                request.file().getBytes(),
                request.file().getContentType(),
                extractExtension(request.file().getOriginalFilename()));

        return commandRunner.run(command);
    }

    @DeleteMapping("/{adventureId}/image")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeAdventureImage(@PathVariable UUID adventureId) {
        commandRunner.run(new RemoveAdventureImage(adventureId));
    }

    @PostMapping("/{adventureId}/invitations")
    @ResponseStatus(code = HttpStatus.OK)
    public InviteUserToAdventureResult invite(
            @PathVariable(required = true) UUID adventureId,
            @Valid @RequestBody InviteUserToAdventureRequest request) {

        return commandRunner.run(new InviteUserToAdventure(
                adventureId, request.usernames(), getAuthenticatedUser().id()));
    }

    @GetMapping("/{adventureId}/invitation")
    @ResponseStatus(code = HttpStatus.OK)
    public PendingAdventureInvitationDetails getPendingInvitation(
            @PathVariable(required = true) UUID adventureId) {

        return queryRunner.run(new GetPendingAdventureInvitation(
                adventureId,
                getAuthenticatedUser().username()));
    }

    @PostMapping("/invitations/{invitationId}/join")
    @ResponseStatus(code = HttpStatus.OK)
    public void join(
            @PathVariable(required = true) UUID invitationId,
            @Valid @RequestBody JoinAdventureWithCharacterRequest request) {

        commandRunner.run(new JoinAdventureWithCharacter(
                invitationId,
                request.playerCharacterId(),
                getAuthenticatedUser().id()));
    }

    @PostMapping("/invitations/{invitationId}/decline")
    @ResponseStatus(code = HttpStatus.OK)
    public void decline(@PathVariable(required = true) UUID invitationId) {
        commandRunner.run(new DeclineAdventureInvitation(invitationId));
    }

    @DeleteMapping("/{adventureId}/characters/{playerCharacterId}")
    @ResponseStatus(code = HttpStatus.OK)
    public void removeCharacter(
            @PathVariable(required = true) UUID adventureId,
            @PathVariable(required = true) UUID playerCharacterId) {

        commandRunner.run(new RemoveCharacterFromAdventure(
                adventureId,
                playerCharacterId,
                getAuthenticatedUser().id()));
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "png";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

}
