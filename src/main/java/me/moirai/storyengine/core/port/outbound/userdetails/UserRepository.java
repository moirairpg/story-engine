package me.moirai.storyengine.core.port.outbound.userdetails;

import java.util.Optional;

import me.moirai.storyengine.core.domain.userdetails.User;

public interface UserRepository {

    Optional<User> findByDiscordId(String discordUserId);

    User save(User user);

    void delete(User user);
}
