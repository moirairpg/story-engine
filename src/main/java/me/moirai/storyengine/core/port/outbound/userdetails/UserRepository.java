package me.moirai.storyengine.core.port.outbound.userdetails;

import java.util.Optional;
import java.util.UUID;

import me.moirai.storyengine.core.domain.userdetails.User;

public interface UserRepository {

    Optional<User> findByDiscordId(String discordUserId);

    Optional<User> findByPublicId(UUID publicId);

    User save(User user);

    void delete(User user);
}
