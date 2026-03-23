package me.moirai.storyengine.core.port.outbound.discord;

import java.util.Optional;

public interface DiscordUserDetailsPort {

    Optional<DiscordUserDetails> getUserById(String userDiscordId);

    Optional<DiscordUserDetails> getGuildMemberById(String userId, String guildId);
}
