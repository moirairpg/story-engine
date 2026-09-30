package me.moirai.storyengine.core.application.command.user;

import me.moirai.storyengine.common.annotation.Authorize;
import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.core.port.inbound.userdetails.UpdateUserDetails;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

@CommandHandler
@Authorize(operation = AuthorizationOperation.UPDATE_USER_DETAILS, fields = "#request.username")
public class UpdateUserDetailsHandler extends AbstractCommandHandler<UpdateUserDetails, Void> {

    private static final String USER_NOT_FOUND = "User with requested username was not found";

    private final UserRepository repository;

    public UpdateUserDetailsHandler(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public Void execute(UpdateUserDetails command) {

        var user = repository.findByUsername(command.username())
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));

        user.updateDisplayName(command.displayName());
        user.updateBio(command.bio());

        repository.save(user);

        return null;
    }
}
