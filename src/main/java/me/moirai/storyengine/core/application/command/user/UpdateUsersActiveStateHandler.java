package me.moirai.storyengine.core.application.command.user;

import java.util.Set;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.port.inbound.userdetails.UpdateUsersActiveState;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

@CommandHandler
public class UpdateUsersActiveStateHandler extends AbstractCommandHandler<UpdateUsersActiveState, Void> {

    private static final String USERS_NOT_FOUND = "One or more of the requested users are not registered in MoirAI";

    private final UserRepository repository;

    public UpdateUsersActiveStateHandler(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public Void execute(UpdateUsersActiveState command) {

        var requestedIds = Set.copyOf(command.userIds());
        var users = repository.findAllByPublicIdIn(requestedIds);

        if (users.size() != requestedIds.size()) {
            throw new NotFoundException(USERS_NOT_FOUND);
        }

        users.forEach(user -> user.updateActiveState(command.isActive(), command.requesterId()));
        users.forEach(repository::save);

        return null;
    }
}
