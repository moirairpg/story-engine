package me.moirai.storyengine.core.application.query.user;

import me.moirai.storyengine.common.annotation.QueryHandler;
import me.moirai.storyengine.common.cqs.query.AbstractQueryHandler;
import me.moirai.storyengine.core.port.inbound.userdetails.GetSignUpDetails;
import me.moirai.storyengine.core.port.inbound.userdetails.SignUpDetailsResult;
import me.moirai.storyengine.core.port.outbound.discord.DiscordAuthenticationPort;

@QueryHandler
public class GetSignUpDetailsHandler extends AbstractQueryHandler<GetSignUpDetails, SignUpDetailsResult> {

    private final DiscordAuthenticationPort discordAuthenticationPort;

    public GetSignUpDetailsHandler(DiscordAuthenticationPort discordAuthenticationPort) {
        this.discordAuthenticationPort = discordAuthenticationPort;
    }

    @Override
    public SignUpDetailsResult execute(GetSignUpDetails useCase) {

        var discordUser = discordAuthenticationPort.getLoggedUser(useCase.discordToken());

        return new SignUpDetailsResult(discordUser.username());
    }
}
