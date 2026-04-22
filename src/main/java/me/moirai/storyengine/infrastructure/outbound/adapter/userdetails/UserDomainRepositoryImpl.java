package me.moirai.storyengine.infrastructure.outbound.adapter.userdetails;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import me.moirai.storyengine.core.domain.userdetails.User;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

@Repository
public class UserDomainRepositoryImpl implements UserRepository {

    private final UserJpaRepository jpaRepository;

    public UserDomainRepositoryImpl(UserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<User> findByDiscordId(String discordUserId) {
        return jpaRepository.findByDiscordId(discordUserId);
    }

    @Override
    public Optional<User> findByPublicId(UUID publicId) {
        return jpaRepository.findByPublicId(publicId);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsername(username);
    }

    @Override
    public User save(User user) {
        return jpaRepository.save(user);
    }

    @Override
    public void delete(User user) {
        jpaRepository.delete(user);
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpaRepository.findById(id);
    }
}
