package me.moirai.storyengine.core.port.outbound.userdetails;

import java.util.Optional;

public interface UserReader {

    Optional<UserData> getUserByDiscordId(String discordId);

    Optional<UserData> getUserByUsername(String username);
}
