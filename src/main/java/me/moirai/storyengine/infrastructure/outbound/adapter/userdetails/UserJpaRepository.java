package me.moirai.storyengine.infrastructure.outbound.adapter.userdetails;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import me.moirai.storyengine.common.dbutil.PaginationRepository;
import me.moirai.storyengine.core.domain.userdetails.User;

public interface UserJpaRepository
                extends JpaRepository<User, Long>, PaginationRepository<User, Long> {

        Optional<User> findByDiscordId(String discordId);

        Optional<User> findByPublicId(UUID publicId);

        @Query("""
                SELECT u
                  FROM User u
                 WHERE LOWER(u.username.name) = LOWER(:username)
                """)
        Optional<User> findByUsername(@Param("username") String username);

        @Query("""
                SELECT u
                  FROM User u
                 WHERE LOWER(u.username.name) IN :usernames
                """)
        List<User> findAllByUsernameIn(@Param("usernames") List<String> usernames);

        void deleteByDiscordId(String discordId);
}