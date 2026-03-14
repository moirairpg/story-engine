package me.moirai.storyengine.infrastructure.outbound.persistence.userdetails;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import me.moirai.storyengine.core.domain.userdetails.User;
import me.moirai.storyengine.core.domain.userdetails.UserDomainRepository;

@Repository
public class UserDomainRepositoryImpl implements UserDomainRepository {

    private final UserJpaRepository jpaRepository;

    public UserDomainRepositoryImpl(UserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<User> findByDiscordId(String discordUserId) {
        return jpaRepository.findByDiscordId(discordUserId);
    }

    @Override
    public User save(User user) {
        return jpaRepository.save(user);
    }

    @Override
    public void delete(User user) {
        jpaRepository.delete(user);
    }
}
