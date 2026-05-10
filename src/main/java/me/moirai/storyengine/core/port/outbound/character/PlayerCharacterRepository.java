package me.moirai.storyengine.core.port.outbound.character;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import me.moirai.storyengine.core.domain.character.PlayerCharacter;

public interface PlayerCharacterRepository {

    PlayerCharacter save(PlayerCharacter character);

    Optional<PlayerCharacter> findByPublicId(UUID publicId);

    Optional<PlayerCharacter> findById(Long id);

    List<PlayerCharacter> findAllByIdIn(List<Long> ids);

    void deleteByPublicId(UUID publicId);
}