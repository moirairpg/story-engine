package me.moirai.storyengine.core.port.inbound.discord.userdetails;

import me.moirai.storyengine.common.usecases.UseCase;

public final class GetUserDetailsByDiscordId extends UseCase<UserDetailsResult> {

    private final String discordUserId;

    private GetUserDetailsByDiscordId(String discordUserId) {
        this.discordUserId = discordUserId;
    }

    public static GetUserDetailsByDiscordId build(String discordUserId) {
        return new GetUserDetailsByDiscordId(discordUserId);
    }

    public String getDiscordUserId() {
        return discordUserId;
    }
}