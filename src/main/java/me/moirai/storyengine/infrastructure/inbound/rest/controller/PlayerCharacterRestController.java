package me.moirai.storyengine.infrastructure.inbound.rest.controller;

import java.io.IOException;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
import me.moirai.storyengine.common.enums.CharacterClass;
import me.moirai.storyengine.common.enums.PlayerCharacterSortField;
import me.moirai.storyengine.common.enums.SortDirection;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.common.web.SecurityContextAware;
import me.moirai.storyengine.core.port.inbound.ImageResult;
import me.moirai.storyengine.core.port.inbound.character.CreatePlayerCharacter;
import me.moirai.storyengine.core.port.inbound.character.DeletePlayerCharacter;
import me.moirai.storyengine.core.port.inbound.character.GetPlayerCharacterById;
import me.moirai.storyengine.core.port.inbound.character.PlayerCharacterDetails;
import me.moirai.storyengine.core.port.inbound.character.PlayerCharacterSummary;
import me.moirai.storyengine.core.port.inbound.character.RemovePlayerCharacterImage;
import me.moirai.storyengine.core.port.inbound.character.SearchPlayerCharacters;
import me.moirai.storyengine.core.port.inbound.character.UpdatePlayerCharacter;
import me.moirai.storyengine.core.port.inbound.character.UploadPlayerCharacterImage;
import me.moirai.storyengine.infrastructure.inbound.rest.request.CreatePlayerCharacterRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.UpdatePlayerCharacterRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.UploadImageRequest;

@RestController
@RequestMapping("/player-characters")
@Tag(name = "Player Characters", description = "Endpoints for managing Player Characters")
public class PlayerCharacterRestController extends SecurityContextAware {

    private final QueryRunner queryRunner;
    private final CommandRunner commandRunner;

    public PlayerCharacterRestController(
            QueryRunner queryRunner,
            CommandRunner commandRunner) {

        this.queryRunner = queryRunner;
        this.commandRunner = commandRunner;
    }

    @GetMapping("/{characterId}")
    @ResponseStatus(HttpStatus.OK)
    @Authorize(operation = AuthorizationOperation.VIEW_PLAYER_CHARACTER, fields = "#characterId")
    public PlayerCharacterDetails getById(@PathVariable UUID characterId) {
        return queryRunner.run(new GetPlayerCharacterById(characterId));
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PaginatedResult<PlayerCharacterSummary> search(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "character_class", required = false) CharacterClass characterClass,
            @RequestParam(name = "sorting_field", required = false) PlayerCharacterSortField sortingField,
            @RequestParam(name = "direction", required = false) SortDirection direction,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size) {

        return queryRunner.run(new SearchPlayerCharacters(
                name,
                characterClass,
                sortingField,
                direction,
                page,
                size,
                getAuthenticatedUser().id()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlayerCharacterDetails create(
            @Valid @RequestBody CreatePlayerCharacterRequest request) {

        return commandRunner.run(new CreatePlayerCharacter(
                request.name(),
                request.characterClass(),
                request.personality(),
                request.physicalDescription(),
                getAuthenticatedUser().id()));
    }

    @PutMapping("/{characterId}")
    @ResponseStatus(HttpStatus.OK)
    @Authorize(operation = AuthorizationOperation.UPDATE_PLAYER_CHARACTER, fields = "#characterId")
    public PlayerCharacterDetails update(
            @PathVariable UUID characterId,
            @Valid @RequestBody UpdatePlayerCharacterRequest request) {

        return commandRunner.run(new UpdatePlayerCharacter(
                characterId,
                request.name(),
                request.characterClass(),
                request.personality(),
                request.physicalDescription()));
    }

    @DeleteMapping("/{characterId}")
    @ResponseStatus(HttpStatus.OK)
    @Authorize(operation = AuthorizationOperation.DELETE_PLAYER_CHARACTER, fields = "#characterId")
    public void delete(@PathVariable UUID characterId) {
        commandRunner.run(new DeletePlayerCharacter(characterId));
    }

    @PutMapping(value = "/{characterId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Authorize(operation = AuthorizationOperation.UPDATE_PLAYER_CHARACTER, fields = "#characterId")
    public ImageResult uploadImage(
            @PathVariable UUID characterId,
            @Valid @ModelAttribute UploadImageRequest request) throws IOException {

        return commandRunner.run(new UploadPlayerCharacterImage(
                characterId,
                request.file().getBytes(),
                request.file().getContentType(),
                extractExtension(request.file().getOriginalFilename())));
    }

    @DeleteMapping("/{characterId}/image")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Authorize(operation = AuthorizationOperation.UPDATE_PLAYER_CHARACTER, fields = "#characterId")
    public void removeImage(@PathVariable UUID characterId) {
        commandRunner.run(new RemovePlayerCharacterImage(characterId));
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "png";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}