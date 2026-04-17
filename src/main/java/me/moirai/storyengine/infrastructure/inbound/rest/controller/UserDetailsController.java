package me.moirai.storyengine.infrastructure.inbound.rest.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.moirai.storyengine.common.annotation.Authorize;
import me.moirai.storyengine.common.cqs.command.CommandRunner;
import me.moirai.storyengine.common.cqs.query.QueryRunner;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.common.web.SecurityContextAware;
import me.moirai.storyengine.core.port.inbound.userdetails.DeleteUserById;
import me.moirai.storyengine.core.port.inbound.userdetails.GetUserDetailsById;
import me.moirai.storyengine.core.port.inbound.userdetails.UpdateUserRole;
import me.moirai.storyengine.core.port.inbound.userdetails.UpdateUserUsername;
import me.moirai.storyengine.core.port.inbound.userdetails.UserDetailsResult;
import me.moirai.storyengine.infrastructure.inbound.rest.request.UpdateUserRoleRequest;
import me.moirai.storyengine.infrastructure.inbound.rest.request.UpdateUserUsernameRequest;

@RestController
@RequestMapping("/user")
@Tag(name = "Users", description = "Endpoints for managing Discord Users that are registered on MoirAI")
public class UserDetailsController extends SecurityContextAware {

    private final QueryRunner queryRunner;
    private final CommandRunner commandRunner;

    public UserDetailsController(
            QueryRunner queryRunner,
            CommandRunner commandRunner) {

        this.queryRunner = queryRunner;
        this.commandRunner = commandRunner;
    }

    @GetMapping("/{userId}")
    @ResponseStatus(code = HttpStatus.OK)
    @Authorize(operation = AuthorizationOperation.MANAGE_USER, fields = "#userId")
    public UserDetailsResult getUserById(@PathVariable(required = true) UUID userId) {

        var query = new GetUserDetailsById(
                userId,
                getAuthenticatedUser().authorizationToken());

        return queryRunner.run(query);
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

    @PatchMapping("/{userId}/role")
    @ResponseStatus(code = HttpStatus.OK)
    @Authorize(operation = AuthorizationOperation.UPDATE_USER_ROLE, fields = "#userId")
    public void updateRole(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRoleRequest request) {

        commandRunner.run(new UpdateUserRole(userId, request.role()));
    }

}
