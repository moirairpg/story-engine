package me.moirai.storyengine.core.application.command.user;

import me.moirai.storyengine.common.annotation.Authorize;
import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.exception.BusinessRuleViolationException;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.core.port.inbound.userdetails.UpdateUserUsername;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

@CommandHandler
@Authorize(operation = AuthorizationOperation.UPDATE_USER_USERNAME)
public class UpdateUsernameHandler extends AbstractCommandHandler<UpdateUserUsername, Void> {

    private static final String USER_NOT_FOUND = "User with requested username was not found";
    private static final String USERNAME_TAKEN = "This username is already taken";

    private final UserRepository repository;

    public UpdateUsernameHandler(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public Void execute(UpdateUserUsername command) {

        var user = repository.findByUsername(command.username())
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));

        repository.findByUsername(command.newUsername())
                .ifPresent(existing -> {
                    throw new BusinessRuleViolationException(USERNAME_TAKEN);
                });

        user.updateUsername(command.newUsername());
        repository.save(user);

        return null;
    }
}
