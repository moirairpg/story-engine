package me.moirai.storyengine.core.application.command.user;

import static io.micrometer.common.util.StringUtils.isBlank;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.core.port.inbound.discord.userdetails.DeleteUserByDiscordId;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

@CommandHandler
public class DeleteUserByDiscordIdHandler extends AbstractCommandHandler<DeleteUserByDiscordId, Void> {

    private static final String USER_NOT_REGISTERED_IN_MOIRAI = "The User with the requested ID is not registered in MoirAI";

    private final UserRepository repository;

    public DeleteUserByDiscordIdHandler(UserRepository repository) {

        this.repository = repository;
    }

    @Override
    public void validate(DeleteUserByDiscordId useCase) {

        if (isBlank(useCase.discordUserId())) {
            throw new IllegalArgumentException("Discord ID cannot be null");
        }
    }

    @Override
    public Void execute(DeleteUserByDiscordId useCase) {

        var discordUser = repository.findByDiscordId(useCase.discordUserId())
                .orElseThrow(() -> new AssetNotFoundException(USER_NOT_REGISTERED_IN_MOIRAI));

        repository.delete(discordUser);

        return null;
    }
}
