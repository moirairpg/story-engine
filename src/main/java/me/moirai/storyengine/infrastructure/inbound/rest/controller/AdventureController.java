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
import me.moirai.storyengine.core.port.inbound.adventure.AdventureDetails;
import me.moirai.storyengine.core.port.inbound.adventure.CreateAdventure;
import me.moirai.storyengine.core.port.inbound.adventure.DeleteAdventure;
import me.moirai.storyengine.core.port.inbound.adventure.GetAdventureById;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventures;
import me.moirai.storyengine.core.port.inbound.adventure.SearchAdventuresResult;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventure;
import me.moirai.storyengine.infrastructure.inbound.rest.request.AdventureSearchParameters;
import me.moirai.storyengine.infrastructure.inbound.rest.request.CreateAdventureRequest;
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

    private final QueryRunner queryRunner;
    private final CommandRunner commandRunner;

    public AdventureController(
            QueryRunner queryRunner,
            CommandRunner commandRunner) {

        this.queryRunner = queryRunner;
        this.commandRunner = commandRunner;
    }

    // TODO reform search request
    @GetMapping
    @ResponseStatus(code = HttpStatus.OK)
    public Mono<SearchAdventuresResult> search(AdventureSearchParameters searchParameters) {

        return mapWithAuthenticatedUser(authenticatedUser -> {

            var query = new SearchAdventures(
                    searchParameters.getName(),
                    searchParameters.getWorld(),
                    searchParameters.getPersona(),
                    searchParameters.getOwnerId(),
                    searchParameters.isMultiplayer(),
                    searchParameters.getPage(),
                    searchParameters.getSize(),
                    getModel(searchParameters.getModel()),
                    getGameMode(searchParameters.getGameMode()),
                    getModeration(searchParameters.getModeration()),
                    getSortingField(searchParameters.getSortingField()),
                    getDirection(searchParameters.getDirection()),
                    getVisibility(searchParameters.getVisibility()),
                    getOperation(searchParameters.getOperation()),
                    authenticatedUser.discordId());

            return queryRunner.run(query);
        });
    }

    @GetMapping("/{adventureId}")
    @ResponseStatus(code = HttpStatus.OK)
    public Mono<AdventureDetails> getAdventureById(
            @PathVariable(required = true) UUID adventureId) {

        return mapWithAuthenticatedUser(authenticatedUser -> {

            var query = new GetAdventureById(
                    adventureId,
                    authenticatedUser.discordId());

            return queryRunner.run(query);
        });
    }

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public Mono<AdventureDetails> createAdventure(
            @Valid @RequestBody CreateAdventureRequest request) {

        return mapWithAuthenticatedUser(authenticatedUser -> {

            var command = new CreateAdventure(
                    request.name(),
                    null,
                    request.worldId(),
                    request.personaId(),
                    request.channelId(),
                    request.visibility(),
                    request.aiModel(),
                    request.moderation(),
                    authenticatedUser.discordId(),
                    request.gameMode(),
                    request.nudge(),
                    request.remember(),
                    request.authorsNote(),
                    request.bump(),
                    request.bumpFrequency(),
                    request.maxTokenLimit(),
                    request.temperature(),
                    request.frequencyPenalty(),
                    request.presencePenalty(),
                    request.logitBias(),
                    request.stopSequences(),
                    request.usersAllowedToWrite(),
                    request.usersAllowedToRead(),
                    request.isMultiplayer());

            return commandRunner.run(command);
        });
    }

    @PutMapping("/{adventureId}")
    @ResponseStatus(code = HttpStatus.OK)
    public Mono<AdventureDetails> updateAdventure(
            @PathVariable(required = true) UUID adventureId,
            @Valid @RequestBody UpdateAdventureRequest request) {

        return mapWithAuthenticatedUser(authenticatedUser -> {

            var command = new UpdateAdventure(
                    adventureId,
                    null,
                    request.adventureStart(),
                    request.name(),
                    request.worldId(),
                    request.personaId(),
                    request.channelId(),
                    request.visibility(),
                    request.aiModel(),
                    request.moderation(),
                    authenticatedUser.discordId(),
                    request.gameMode(),
                    request.nudge(),
                    request.remember(),
                    request.authorsNote(),
                    request.bump(),
                    request.bumpFrequency(),
                    request.maxTokenLimit(),
                    request.temperature(),
                    request.frequencyPenalty(),
                    request.presencePenalty(),
                    request.logitBiasToAdd(),
                    request.stopSequencesToAdd(),
                    request.stopSequencesToRemove(),
                    request.logitBiasToRemove(),
                    request.usersAllowedToWriteToAdd(),
                    request.usersAllowedToWriteToRemove(),
                    request.usersAllowedToReadToAdd(),
                    request.usersAllowedToReadToRemove(),
                    request.isMultiplayer());

            return commandRunner.run(command);
        });
    }

    @DeleteMapping("/{adventureId}")
    @ResponseStatus(code = HttpStatus.OK)
    public Mono<Void> deleteAdventure(
            @PathVariable(required = true) UUID adventureId) {

        return flatMapWithAuthenticatedUser(authenticatedUser -> {

            var command = new DeleteAdventure(
                    adventureId,
                    authenticatedUser.discordId());

            commandRunner.run(command);

            return Mono.empty();
        });
    }

    // TODO remove all of this
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
