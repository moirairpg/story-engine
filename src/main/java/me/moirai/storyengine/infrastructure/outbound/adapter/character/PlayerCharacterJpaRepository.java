package me.moirai.storyengine.infrastructure.outbound.adapter.character;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import me.moirai.storyengine.core.domain.character.PlayerCharacter;

@Repository
public interface PlayerCharacterJpaRepository extends JpaRepository<PlayerCharacter, Long> {

    Optional<PlayerCharacter> findByPublicId(UUID publicId);

    List<PlayerCharacter> findAllByIdIn(List<Long> ids);

    void deleteByPublicId(UUID publicId);
}