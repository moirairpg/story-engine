package me.moirai.storyengine.core.application.command.user;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.port.inbound.userdetails.UpdateUser;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

@CommandHandler
public class UpdateUserHandler extends AbstractCommandHandler<UpdateUser, Void> {

    private static final String USER_NOT_FOUND = "User with requested ID was not found";

    private final UserRepository repository;

    public UpdateUserHandler(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public void validate(UpdateUser command) {

        if (command.userId() == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        if (command.role() == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
    }

    @Override
    public Void execute(UpdateUser command) {

        var user = repository.findByPublicId(command.userId())
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));

        user.updateRole(command.role(), command.requesterId());
        user.updateActiveState(command.isActive(), command.requesterId());
        user.updateBio(command.bio());

        repository.save(user);

        return null;
    }
}
