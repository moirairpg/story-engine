package me.moirai.storyengine.core.application.command.user;

import java.util.Locale;
import java.util.stream.Collectors;

import me.moirai.storyengine.common.annotation.Authorize;
import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.core.port.inbound.userdetails.UpdateUsersActiveState;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

@CommandHandler
@Authorize(operation = AuthorizationOperation.UPDATE_USERS_ACTIVE_STATE)
public class UpdateUsersActiveStateHandler extends AbstractCommandHandler<UpdateUsersActiveState, Void> {

    private static final String USERS_NOT_FOUND = "One or more of the requested users are not registered in MoirAI";

    private final UserRepository repository;

    public UpdateUsersActiveStateHandler(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public Void execute(UpdateUsersActiveState command) {

        var users = repository.findAllByUsernameIn(command.usernames());

        var foundUsernames = users.stream()
                .map(user -> user.getUsername().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());

        var isAnyMissing = command.usernames().stream()
                .anyMatch(username -> !foundUsernames.contains(username.toLowerCase(Locale.ROOT)));

        if (isAnyMissing) {
            throw new NotFoundException(USERS_NOT_FOUND);
        }

        users.forEach(user -> user.updateActiveState(command.isActive(), command.requesterUsername()));
        users.forEach(repository::save);

        return null;
    }
}
