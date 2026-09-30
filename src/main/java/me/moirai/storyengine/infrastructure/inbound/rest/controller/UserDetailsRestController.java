package me.moirai.storyengine.infrastructure.inbound.rest.controller;

import java.time.Instant;

import org.springframework.format.annotation.DateTimeFormat;
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
import me.moirai.storyengine.common.cqs.command.CommandRunner;
import me.moirai.storyengine.common.cqs.query.QueryRunner;
import me.moirai.storyengine.common.dto.PaginatedResult;
import me.moirai.storyengine.common.enums.Role;
import me.moirai.storyengine.common.enums.SortDirection;
import me.moirai.storyengine.common.web.SecurityContextAware;
import me.moirai.storyengine.core.port.inbound.userdetails.DeleteUserByUsername;
import me.moirai.storyengine.core.port.inbound.userdetails.DeleteUsers;
import me.moirai.storyengine.core.port.inbound.userdetails.DeleteUsersResult;
import me.moirai.storyengine.core.port.inbound.userdetails.GetUserDetailsByUsername;
import me.moirai.storyengine.core.port.inbound.userdetails.SearchUsers;
import me.moirai.storyengine.core.port.inbound.userdetails.UpdateUser;
import me.moirai.storyengine.core.port.inbound.userdetails.UpdateUserDetails;
import me.moirai.storyengine.core.port.inbound.userdetails.UpdateUsersActiveState;
import me.moirai.storyengine.core.port.inbound.userdetails.UpdateUserUsername;
import me.moirai.storyengine.core.port.inbound.userdetails.UserDetailsResult;
import me.moirai.storyengine.core.port.inbound.userdetails.UserSortField;
import me.moirai.storyengine.core.port.inbound.userdetails.UserSummary;
import me.moirai.storyengine.infrastructure.inbound.rest.request.DeleteUsersRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.UpdateUserDetailsRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.UpdateUserRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.UpdateUsersActiveStateRequest;
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
    public PaginatedResult<UserSummary> searchUsers(
            @RequestParam(required = false) String username,
            @RequestParam(name = "display_name", required = false) String displayName,
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
                displayName,
                role,
                isActive,
                registeredFrom,
                registeredTo,
                sortingField,
                direction,
                page,
                size));
    }

    @GetMapping("/{username}")
    @ResponseStatus(code = HttpStatus.OK)
    public UserDetailsResult getUserByUsername(@PathVariable(required = true) String username) {

        return queryRunner.run(new GetUserDetailsByUsername(username));
    }

    @DeleteMapping("/{username}")
    @ResponseStatus(code = HttpStatus.OK)
    public void deleteUserByUsername(@PathVariable(required = true) String username) {

        var command = new DeleteUserByUsername(username);
        commandRunner.run(command);
    }

    @PatchMapping("/{username}/username")
    @ResponseStatus(code = HttpStatus.OK)
    public void updateUsername(
            @PathVariable String username,
            @Valid @RequestBody UpdateUserUsernameRequest request) {

        commandRunner.run(new UpdateUserUsername(username, request.username()));
    }

    @PutMapping("/{username}")
    @ResponseStatus(code = HttpStatus.OK)
    public void updateUser(
            @PathVariable String username,
            @Valid @RequestBody UpdateUserRequest request) {

        commandRunner.run(new UpdateUser(
                username,
                request.role(),
                request.isActive(),
                request.bio(),
                request.displayName(),
                authenticatedUsername()));
    }

    @PutMapping("/{username}/user-details")
    @ResponseStatus(code = HttpStatus.OK)
    public void updateUserDetails(
            @PathVariable String username,
            @Valid @RequestBody UpdateUserDetailsRequest request) {

        commandRunner.run(new UpdateUserDetails(
                username,
                request.displayName(),
                request.bio()));
    }

    @PatchMapping("/active-status")
    @ResponseStatus(code = HttpStatus.OK)
    public void updateUsersActiveState(@Valid @RequestBody UpdateUsersActiveStateRequest request) {

        commandRunner.run(new UpdateUsersActiveState(
                request.usernames(),
                request.isActive(),
                authenticatedUsername()));
    }

    @PostMapping("/deletion")
    @ResponseStatus(code = HttpStatus.OK)
    public DeleteUsersResult deleteUsers(@Valid @RequestBody DeleteUsersRequest request) {

        return commandRunner.run(new DeleteUsers(request.usernames(), authenticatedUsername()));
    }

}
