package me.moirai.storyengine.core.application.command.user;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.core.port.inbound.discord.userdetails.DeleteUserById;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

@CommandHandler
public class DeleteUserByIdHandler extends AbstractCommandHandler<DeleteUserById, Void> {

    private static final String USER_NOT_REGISTERED_IN_MOIRAI = "The User with the requested ID is not registered in MoirAI";

    private final UserRepository repository;

    public DeleteUserByIdHandler(UserRepository repository) {

        this.repository = repository;
    }

    @Override
    public void validate(DeleteUserById useCase) {

        if (useCase.userId() == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
    }

    @Override
    public Void execute(DeleteUserById useCase) {

        var discordUser = repository.findByPublicId(useCase.userId())
                .orElseThrow(() -> new AssetNotFoundException(USER_NOT_REGISTERED_IN_MOIRAI));

        repository.delete(discordUser);

        return null;
    }
}
