package me.moirai.storyengine.infrastructure.outbound.persistence.userdetails;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import me.moirai.storyengine.core.domain.userdetails.User;
import me.moirai.storyengine.infrastructure.outbound.persistence.PaginationRepository;

public interface UserJpaRepository
                extends JpaRepository<User, String>, PaginationRepository<User, String> {

        Optional<User> findByDiscordId(String discordId);

        void deleteByDiscordId(String discordId);
}