package me.moirai.storyengine.infrastructure.outbound.adapter.userdetails;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import me.moirai.storyengine.common.dbutil.PaginationRepository;
import me.moirai.storyengine.core.domain.userdetails.User;

public interface UserJpaRepository
                extends JpaRepository<User, Long>, PaginationRepository<User, Long> {

        Optional<User> findByDiscordId(String discordId);

        Optional<User> findByPublicId(UUID publicId);

        Optional<User> findByUsername(String username);

        List<User> findAllByUsernameIn(Collection<String> usernames);

        void deleteByDiscordId(String discordId);
}