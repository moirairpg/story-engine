package me.moirai.storyengine.core.application.query.user;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.util.Optional;

import me.moirai.storyengine.common.annotation.QueryHandler;
import me.moirai.storyengine.common.cqs.query.AbstractQueryHandler;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.exception.DiscordApiException;
import me.moirai.storyengine.core.port.inbound.discord.userdetails.GetUserDetailsById;
import me.moirai.storyengine.core.port.inbound.discord.userdetails.UserDetailsResult;
import me.moirai.storyengine.core.port.outbound.discord.DiscordUserDetailsPort;
import me.moirai.storyengine.core.port.outbound.userdetails.UserReader;

@QueryHandler
public class GetUserDetailsByIdHandler extends AbstractQueryHandler<GetUserDetailsById, UserDetailsResult> {

    private static final String USER_NOT_REGISTERED_IN_MOIRAI = "The User with the requested ID is not registered in MoirAI";
    private static final String DISCORD_USER_DOES_NOT_EXIST = "The Discord User with the requested ID does not exist";

    private final UserReader userReader;
    private final DiscordUserDetailsPort discordUserDetailsPort;

    public GetUserDetailsByIdHandler(
            UserReader userReader,
            DiscordUserDetailsPort discordUserDetailsPort) {

        this.userReader = userReader;
        this.discordUserDetailsPort = discordUserDetailsPort;
    }

    @Override
    public void validate(GetUserDetailsById useCase) {

        if (useCase.userId() == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
    }

    @Override
    public UserDetailsResult execute(GetUserDetailsById useCase) {

        var dbData = userReader.getUserById(useCase.userId())
                .orElseThrow(() -> new AssetNotFoundException(USER_NOT_REGISTERED_IN_MOIRAI));

        var discordDetails = discordUserDetailsPort.getUserById(dbData.discordId())
                .orElseThrow(() -> new DiscordApiException(NOT_FOUND, DISCORD_USER_DOES_NOT_EXIST));

        var nickname = Optional.ofNullable(discordDetails.getNickname())
                .orElse(discordDetails.getUsername());

        return new UserDetailsResult(
                dbData.publicId(),
                dbData.id(),
                dbData.discordId(),
                discordDetails.getUsername(),
                nickname,
                discordDetails.getAvatarUrl(),
                dbData.role(),
                dbData.creationDate());
    }
}
