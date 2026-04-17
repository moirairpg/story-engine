package me.moirai.storyengine.core.port.outbound.discord;

import java.util.Optional;

public interface DiscordUserDetailsPort {

    Optional<DiscordUserDataResponse> getUserById(String userDiscordId, String token);
}
