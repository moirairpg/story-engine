package me.moirai.storyengine.infrastructure.inbound.rest.controller;

import java.time.Instant;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
import me.moirai.storyengine.common.enums.Role;
import me.moirai.storyengine.common.enums.SortDirection;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.common.web.SecurityContextAware;
import me.moirai.storyengine.core.port.inbound.userdetails.DeleteUserById;
import me.moirai.storyengine.core.port.inbound.userdetails.GetUserDetailsById;
import me.moirai.storyengine.core.port.inbound.userdetails.SearchUsers;
import me.moirai.storyengine.core.port.inbound.userdetails.UpdateUser;
import me.moirai.storyengine.core.port.inbound.userdetails.UpdateUserUsername;
import me.moirai.storyengine.core.port.inbound.userdetails.UserDetailsResult;
import me.moirai.storyengine.core.port.inbound.userdetails.UserSortField;
import me.moirai.storyengine.core.port.inbound.userdetails.UserSummary;
import me.moirai.storyengine.infrastructure.inbound.rest.request.UpdateUserRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.UpdateUserUsernameRequest;

@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "Endpoints for managing Discord Users that are registered on MoirAI")
public class UserDetailsRestController extends SecurityContextAware {

    private final QueryRunner queryRunner;
    private final CommandRunner commandRunner;

    public UserDetailsRestController(
            QueryRunner queryRunner,
            CommandRunner commandRunner) {

        this.queryRunner = queryRunner;
        this.commandRunner = commandRunner;
    }

    @GetMapping
    @ResponseStatus(code = HttpStatus.OK)
    @Authorize(operation = AuthorizationOperation.SEARCH_USERS)
    public PaginatedResult<UserSummary> searchUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Role role,
            @RequestParam(name = "is_active", required = false) Boolean isActive,
            @RequestParam(name = "registered_from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant registeredFrom,
            @RequestParam(name = "registered_to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant registeredTo,
            @RequestParam(name = "sorting_field", required = false) UserSortField sortingField,
            @RequestParam(name = "sorting_direction", required = false) SortDirection direction,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        return queryRunner.run(new SearchUsers(
                username,
                role,
                isActive,
                registeredFrom,
                registeredTo,
                sortingField,
                direction,
                page,
                size));
    }

    @GetMapping("/{userId}")
    @ResponseStatus(code = HttpStatus.OK)
    @Authorize(operation = AuthorizationOperation.MANAGE_USER, fields = "#userId")
    public UserDetailsResult getUserById(@PathVariable(required = true) UUID userId) {

        return queryRunner.run(new GetUserDetailsById(userId));
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(code = HttpStatus.OK)
    @Authorize(operation = AuthorizationOperation.MANAGE_USER, fields = "#userId")
    public void deleteUserById(@PathVariable(required = true) UUID userId) {

        var command = new DeleteUserById(userId);
        commandRunner.run(command);
    }

    @PatchMapping("/{userId}/username")
    @ResponseStatus(code = HttpStatus.OK)
    @Authorize(operation = AuthorizationOperation.UPDATE_USER_USERNAME, fields = "#userId")
    public void updateUsername(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserUsernameRequest request) {

        commandRunner.run(new UpdateUserUsername(userId, request.username()));
    }

    @PatchMapping("/{userId}")
    @ResponseStatus(code = HttpStatus.OK)
    @Authorize(operation = AuthorizationOperation.UPDATE_USER, fields = "#userId")
    public void updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRequest request) {

        commandRunner.run(new UpdateUser(
                userId,
                request.role(),
                request.isActive(),
                request.bio(),
                authenticatedUserId()));
    }

}
