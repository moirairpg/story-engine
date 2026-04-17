package me.moirai.storyengine.core.port.outbound.userdetails;

import java.util.Optional;
import java.util.UUID;

import me.moirai.storyengine.core.port.inbound.userdetails.UserData;

public interface UserReader {

    Optional<UserData> getUserByDiscordId(String discordId);

    Optional<UserData> getUserById(UUID id);
}
