package me.moirai.storyengine.core.port.outbound.userdetails;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import me.moirai.storyengine.core.domain.userdetails.User;

public interface UserRepository {

    Optional<User> findByDiscordId(String discordUserId);

    Optional<User> findById(Long id);

    Optional<User> findByPublicId(UUID publicId);

    Optional<User> findByUsername(String username);

    List<User> findAllById(Collection<Long> ids);

    List<User> findAllByUsernameIn(Collection<String> usernames);

    User save(User user);

    void delete(User user);
}
