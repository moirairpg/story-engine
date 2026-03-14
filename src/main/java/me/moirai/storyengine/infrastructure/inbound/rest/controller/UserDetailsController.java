package me.moirai.storyengine.infrastructure.inbound.rest.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import me.moirai.storyengine.common.usecases.UseCaseRunner;
import me.moirai.storyengine.common.web.SecurityContextAware;
import me.moirai.storyengine.core.application.usecase.discord.userdetails.request.DeleteUserByDiscordId;
import me.moirai.storyengine.core.application.usecase.discord.userdetails.request.GetUserDetailsByDiscordId;
import me.moirai.storyengine.core.application.usecase.notification.request.GetNotificationsByUserId;
import me.moirai.storyengine.infrastructure.inbound.rest.mapper.NotificationResponseMapper;
import me.moirai.storyengine.infrastructure.inbound.rest.mapper.UserDataResponseMapper;
import me.moirai.storyengine.infrastructure.inbound.rest.response.NotificationResponse;
import me.moirai.storyengine.infrastructure.inbound.rest.response.UserDataResponse;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/user")
@Tag(name = "Users", description = "Endpoints for managing Discord Users that are registered on MoirAI")
public class UserDetailsController extends SecurityContextAware {

    private final UseCaseRunner useCaseRunner;
    private final UserDataResponseMapper userDataResponseMapper;
    private final NotificationResponseMapper notificationResponseMapper;

    public UserDetailsController(UseCaseRunner useCaseRunner,
            UserDataResponseMapper userDataResponseMapper,
            NotificationResponseMapper notificationResponseMapper) {

        this.useCaseRunner = useCaseRunner;
        this.userDataResponseMapper = userDataResponseMapper;
        this.notificationResponseMapper = notificationResponseMapper;
    }

    @GetMapping("/{discordUserId}")
    @ResponseStatus(code = HttpStatus.OK)
    @PreAuthorize("isAdmin()")
    public Mono<UserDataResponse> getUserByDiscordId(@PathVariable(required = true) String discordUserId) {

        return Mono.just(GetUserDetailsByDiscordId.build(discordUserId))
                .map(useCaseRunner::run)
                .map(userDataResponseMapper::toResponse);
    }

    @DeleteMapping("/{discordUserId}")
    @ResponseStatus(code = HttpStatus.OK)
    @PreAuthorize("isAdmin() || isAuthenticatedUser(#discordUserId)")
    public void deleteUserByDiscordId(@PathVariable(required = true) String discordUserId) {

        DeleteUserByDiscordId command = DeleteUserByDiscordId.build(discordUserId);
        useCaseRunner.run(command);
    }

    @GetMapping("/{discordUserId}/notifications")
    @ResponseStatus(code = HttpStatus.OK)
    @PreAuthorize("isAdmin() || isAuthenticatedUser(#discordUserId)")
    public Mono<List<NotificationResponse>> getNotificationsByUserId(
            @PathVariable(required = true) String discordUserId) {

        return mapWithAuthenticatedUser(authenticatedUser -> {

            GetNotificationsByUserId request = GetNotificationsByUserId.create(discordUserId);
            return useCaseRunner.run(request).stream()
                    .map(notificationResponseMapper::toResponse)
                    .toList();
        });
    }
}
