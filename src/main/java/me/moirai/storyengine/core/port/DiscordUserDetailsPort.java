package me.moirai.storyengine.core.port;

import java.util.Optional;

import me.moirai.storyengine.core.application.usecase.discord.DiscordUserDetails;

public interface DiscordUserDetailsPort {

    Optional<DiscordUserDetails> getUserById(String userDiscordId);

    Optional<DiscordUserDetails> getGuildMemberById(String userId, String guildId);

    DiscordUserDetails getBotUser();
}
