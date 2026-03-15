package me.moirai.storyengine.infrastructure.outbound.adapter.userdetails;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import me.moirai.storyengine.common.dbutil.PaginationRepository;
import me.moirai.storyengine.core.domain.userdetails.User;

public interface UserJpaRepository
                extends JpaRepository<User, Long>, PaginationRepository<User, Long> {

        Optional<User> findByDiscordId(String discordId);

        void deleteByDiscordId(String discordId);
}