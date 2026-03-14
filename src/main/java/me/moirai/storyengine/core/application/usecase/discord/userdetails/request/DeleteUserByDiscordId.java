package me.moirai.storyengine.core.application.usecase.discord.userdetails.request;

import me.moirai.storyengine.common.usecases.UseCase;

public final class DeleteUserByDiscordId extends UseCase<Void> {

    private final String discordUserId;

    private DeleteUserByDiscordId(String discordUserId) {
        this.discordUserId = discordUserId;
    }

    public static DeleteUserByDiscordId build(String discordUserId) {
        return new DeleteUserByDiscordId(discordUserId);
    }

    public String getDiscordUserId() {
        return discordUserId;
    }
}