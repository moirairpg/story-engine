package me.moirai.storyengine.core.domain.userdetails;

import java.util.Optional;

public interface UserDomainRepository {

    Optional<User> findByDiscordId(String discordUserId);

    User save(User user);

    void delete(User user);
}
