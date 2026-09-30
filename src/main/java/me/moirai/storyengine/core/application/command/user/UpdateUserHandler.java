package me.moirai.storyengine.core.application.command.user;

import me.moirai.storyengine.common.annotation.Authorize;
import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.core.port.inbound.userdetails.UpdateUser;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

@CommandHandler
@Authorize(operation = AuthorizationOperation.UPDATE_USER)
public class UpdateUserHandler extends AbstractCommandHandler<UpdateUser, Void> {

    private static final String USER_NOT_FOUND = "User with requested username was not found";

    private final UserRepository repository;

    public UpdateUserHandler(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public Void execute(UpdateUser command) {

        var user = repository.findByUsername(command.username())
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));

        user.updateRole(command.role(), command.requesterUsername());
        user.updateActiveState(command.isActive(), command.requesterUsername());
        user.updateBio(command.bio());
        user.updateDisplayName(command.displayName());

        repository.save(user);

        return null;
    }
}
