package me.moirai.storyengine.core.application.query.user;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import me.moirai.storyengine.common.annotation.Authorize;
import me.moirai.storyengine.common.annotation.QueryHandler;
import me.moirai.storyengine.common.cqs.query.AbstractQueryHandler;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.common.exception.RestException;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.core.port.inbound.userdetails.GetUserDetailsByUsername;
import me.moirai.storyengine.core.port.inbound.userdetails.UserDetailsResult;
import me.moirai.storyengine.core.port.outbound.discord.DiscordUserDetailsPort;
import me.moirai.storyengine.core.port.outbound.userdetails.UserReader;

@QueryHandler
@Authorize(operation = AuthorizationOperation.MANAGE_USER, fields = "#request.username")
public class GetUserDetailsByUsernameHandler
        extends AbstractQueryHandler<GetUserDetailsByUsername, UserDetailsResult> {

    private static final String USER_NOT_REGISTERED_IN_MOIRAI = "The User with the requested username is not registered in MoirAI";
    private static final String DISCORD_USER_DOES_NOT_EXIST = "The Discord User with the requested ID does not exist";

    private final UserReader userReader;
    private final DiscordUserDetailsPort discordUserDetailsPort;

    public GetUserDetailsByUsernameHandler(
            UserReader userReader,
            DiscordUserDetailsPort discordUserDetailsPort) {

        this.userReader = userReader;
        this.discordUserDetailsPort = discordUserDetailsPort;
    }

    @Override
    public UserDetailsResult execute(GetUserDetailsByUsername useCase) {

        var moiraiUserDetails = userReader.getUserByUsername(useCase.username())
                .orElseThrow(() -> new NotFoundException(USER_NOT_REGISTERED_IN_MOIRAI));

        var discordUserDetails = discordUserDetailsPort.getUserById(moiraiUserDetails.discordId())
                .orElseThrow(() -> new RestException(NOT_FOUND, DISCORD_USER_DOES_NOT_EXIST));

        return new UserDetailsResult(
                moiraiUserDetails.publicId(),
                moiraiUserDetails.id(),
                discordUserDetails.username(),
                moiraiUserDetails.username(),
                moiraiUserDetails.displayName(),
                discordUserDetails.avatarUrl(),
                moiraiUserDetails.role(),
                moiraiUserDetails.isActive(),
                moiraiUserDetails.bio(),
                moiraiUserDetails.creationDate());
    }
}
