package me.moirai.storyengine.core.application.command.user;

import static me.moirai.storyengine.common.enums.Role.PLAYER;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.exception.BusinessRuleViolationException;
import me.moirai.storyengine.core.domain.userdetails.User;
import me.moirai.storyengine.core.port.inbound.userdetails.CreateUser;
import me.moirai.storyengine.core.port.outbound.discord.DiscordAuthenticationPort;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

@CommandHandler
public class CreateUserHandler extends AbstractCommandHandler<CreateUser, Void> {

    private static final String USERNAME_TAKEN = "This username is already taken";

    private final UserRepository repository;
    private final DiscordAuthenticationPort discordAuthenticationPort;

    public CreateUserHandler(
            UserRepository repository,
            DiscordAuthenticationPort discordAuthenticationPort) {

        this.repository = repository;
        this.discordAuthenticationPort = discordAuthenticationPort;
    }

    @Override
    public Void execute(CreateUser command) {

        var discordUser = discordAuthenticationPort.getLoggedUser(command.discordToken());

        if (repository.findByDiscordId(discordUser.id()).isPresent()) {
            return null;
        }

        repository.findByUsername(command.username())
                .ifPresent(existing -> {
                    throw new BusinessRuleViolationException(USERNAME_TAKEN);
                });

        repository.save(User.builder()
                .discordId(discordUser.id())
                .username(command.username())
                .displayName(command.displayName())
                .role(PLAYER)
                .build());

        return null;
    }
}
