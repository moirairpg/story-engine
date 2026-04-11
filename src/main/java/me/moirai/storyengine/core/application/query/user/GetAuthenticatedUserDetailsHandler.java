package me.moirai.storyengine.core.application.query.user;

import java.util.Optional;

import me.moirai.storyengine.common.annotation.QueryHandler;
import me.moirai.storyengine.common.cqs.query.AbstractQueryHandler;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.port.inbound.userdetails.GetAuthenticatedUserDetails;
import me.moirai.storyengine.core.port.inbound.userdetails.UserDetailsResult;
import me.moirai.storyengine.core.port.outbound.discord.DiscordAuthenticationPort;
import me.moirai.storyengine.core.port.outbound.userdetails.UserReader;

@QueryHandler
public class GetAuthenticatedUserDetailsHandler extends AbstractQueryHandler<GetAuthenticatedUserDetails, UserDetailsResult> {

    private static final String USER_NOT_REGISTERED_IN_MOIRAI = "The User with the requested ID is not registered in MoirAI";

    private final UserReader userReader;
    private final DiscordAuthenticationPort discordAuthenticationPort;

    public GetAuthenticatedUserDetailsHandler(
            UserReader userReader,
            DiscordAuthenticationPort discordAuthenticationPort) {

        this.userReader = userReader;
        this.discordAuthenticationPort = discordAuthenticationPort;
    }

    @Override
    public UserDetailsResult execute(GetAuthenticatedUserDetails useCase) {

        var discordUserDetails = discordAuthenticationPort.getLoggedUser(useCase.discordToken());

        var moiraiUserDetails = userReader.getUserByDiscordId(discordUserDetails.id())
                .orElseThrow(() -> new NotFoundException(USER_NOT_REGISTERED_IN_MOIRAI));

        var nickname = Optional.ofNullable(discordUserDetails.globalNickname())
                .orElse(discordUserDetails.username());

        return new UserDetailsResult(
                moiraiUserDetails.publicId(),
                moiraiUserDetails.id(),
                moiraiUserDetails.discordId(),
                discordUserDetails.username(),
                nickname,
                discordUserDetails.avatarUrl(),
                moiraiUserDetails.role(),
                moiraiUserDetails.creationDate());
    }
}
