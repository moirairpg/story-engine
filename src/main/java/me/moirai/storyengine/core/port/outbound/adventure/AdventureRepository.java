package me.moirai.storyengine.core.port.outbound.adventure;

import java.util.Optional;
import java.util.UUID;

import me.moirai.storyengine.core.domain.adventure.Adventure;

public interface AdventureRepository {

    Adventure save(Adventure adventure);

    void deleteByPublicId(UUID publicId);

    void updateSceneByPublicId(String scene, UUID publicId);

    void updateAuthorsNoteByPublicId(String authorsNote, UUID publicId);

    void updateNudgeByPublicId(String nudge, UUID publicId);

    void updateBumpByPublicId(String bumpContent, int bumpFrequency, UUID publicId);

    Optional<Adventure> findByPublicId(UUID publicId);

    Optional<Adventure> findById(Long id);
}
