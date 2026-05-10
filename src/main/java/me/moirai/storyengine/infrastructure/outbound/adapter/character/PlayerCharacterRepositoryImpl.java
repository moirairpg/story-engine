package me.moirai.storyengine.infrastructure.outbound.adapter.character;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import me.moirai.storyengine.core.domain.character.PlayerCharacter;
import me.moirai.storyengine.core.port.outbound.character.PlayerCharacterRepository;

@Repository
public class PlayerCharacterRepositoryImpl implements PlayerCharacterRepository {

    private final PlayerCharacterJpaRepository jpaRepository;

    public PlayerCharacterRepositoryImpl(PlayerCharacterJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public PlayerCharacter save(PlayerCharacter character) {
        return jpaRepository.save(character);
    }

    @Override
    public Optional<PlayerCharacter> findByPublicId(UUID publicId) {
        return jpaRepository.findByPublicId(publicId);
    }

    @Override
    public Optional<PlayerCharacter> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<PlayerCharacter> findAllByIdIn(List<Long> ids) {
        return jpaRepository.findAllByIdIn(ids);
    }

    @Override
    public void deleteByPublicId(UUID publicId) {
        jpaRepository.deleteByPublicId(publicId);
    }
}
