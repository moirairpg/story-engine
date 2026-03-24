package me.moirai.storyengine.core.application.query.user;

import static io.micrometer.common.util.StringUtils.isBlank;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.util.Optional;

import me.moirai.storyengine.common.annotation.QueryHandler;
import me.moirai.storyengine.common.cqs.query.AbstractQueryHandler;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.exception.DiscordApiException;
import me.moirai.storyengine.core.port.inbound.discord.userdetails.GetUserDetailsByDiscordId;
import me.moirai.storyengine.core.port.inbound.discord.userdetails.UserDetailsResult;
import me.moirai.storyengine.core.port.outbound.discord.DiscordUserDetailsPort;
import me.moirai.storyengine.core.port.outbound.userdetails.UserReader;

@QueryHandler
public class GetUserDetailsByDiscordIdHandler
        extends AbstractQueryHandler<GetUserDetailsByDiscordId, UserDetailsResult> {

    private static final String USER_NOT_REGISTERED_IN_MOIRAI = "The User with the requested ID is not registered in MoirAI";
    private static final String DISCORD_USER_DOES_NOT_EXIST = "The Discord User with the requested ID does not exist";

    private final UserReader userReader;
    private final DiscordUserDetailsPort discordUserDetailsPort;

    public GetUserDetailsByDiscordIdHandler(
            UserReader userReader,
            DiscordUserDetailsPort discordUserDetailsPort) {

        this.userReader = userReader;
        this.discordUserDetailsPort = discordUserDetailsPort;
    }

    @Override
    public void validate(GetUserDetailsByDiscordId useCase) {

        if (isBlank(useCase.discordUserId())) {
            throw new IllegalArgumentException("Discord ID cannot be null");
        }
    }

    @Override
    public UserDetailsResult execute(GetUserDetailsByDiscordId useCase) {

        var discordDetails = discordUserDetailsPort.getUserById(useCase.discordUserId())
                .orElseThrow(() -> new DiscordApiException(NOT_FOUND, DISCORD_USER_DOES_NOT_EXIST));

        var dbData = userReader.getUserByDiscordId(useCase.discordUserId())
                .orElseThrow(() -> new AssetNotFoundException(USER_NOT_REGISTERED_IN_MOIRAI));

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
