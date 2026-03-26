package me.moirai.storyengine.core.application.command.user;

import static io.micrometer.common.util.StringUtils.isBlank;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.core.port.inbound.userdetails.UpdateUserUsername;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

@CommandHandler
public class UpdateUsernameHandler extends AbstractCommandHandler<UpdateUserUsername, Void> {

    private static final String USER_NOT_FOUND = "User with requested ID was not found";

    private final UserRepository repository;

    public UpdateUsernameHandler(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public void validate(UpdateUserUsername command) {
        if (command.userId() == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        if (isBlank(command.username())) {
            throw new IllegalArgumentException("Username cannot be blank");
        }
    }

    @Override
    public Void execute(UpdateUserUsername command) {
        var user = repository.findByPublicId(command.userId())
                .orElseThrow(() -> new AssetNotFoundException(USER_NOT_FOUND));

        user.updateUsername(command.username());
        repository.save(user);

        return null;
    }
}
