package me.moirai.storyengine.infrastructure.inbound.rest.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import me.moirai.storyengine.common.cqs.command.CommandRunner;
import me.moirai.storyengine.common.cqs.query.QueryRunner;
import me.moirai.storyengine.common.web.SecurityContextAware;
import me.moirai.storyengine.core.port.inbound.discord.userdetails.DeleteUserByDiscordId;
import me.moirai.storyengine.core.port.inbound.discord.userdetails.GetUserDetailsByDiscordId;
import me.moirai.storyengine.core.port.inbound.discord.userdetails.UserDetailsResult;
import reactor.core.publisher.Mono;

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

    @GetMapping("/{discordUserId}")
    @ResponseStatus(code = HttpStatus.OK)
    @PreAuthorize("isAdmin()")
    public Mono<UserDetailsResult> getUserByDiscordId(@PathVariable(required = true) String discordUserId) {

        return Mono.just(new GetUserDetailsByDiscordId(discordUserId))
                .map(queryRunner::run);
    }

    @DeleteMapping("/{discordUserId}")
    @ResponseStatus(code = HttpStatus.OK)
    @PreAuthorize("isAdmin() || isAuthenticatedUser(#discordUserId)")
    public void deleteUserByDiscordId(@PathVariable(required = true) String discordUserId) {

        var command = new DeleteUserByDiscordId(discordUserId);
        commandRunner.run(command);
    }

}
