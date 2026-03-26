package me.moirai.storyengine.infrastructure.outbound.adapter.adventure;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import me.moirai.storyengine.common.dbutil.PaginationRepository;
import me.moirai.storyengine.core.domain.adventure.Adventure;

public interface AdventureJpaRepository
        extends JpaRepository<Adventure, Long>, PaginationRepository<Adventure, Long> {

    Optional<Adventure> findByPublicId(UUID publicId);

    void deleteByPublicId(UUID publicId);

    @Modifying
    @Query("UPDATE Adventure a SET a.contextAttributes.scene = :scene WHERE a.publicId = :publicId")
    void updateSceneByPublicId(String scene, UUID publicId);

    @Modifying
    @Query("UPDATE Adventure a SET a.contextAttributes.authorsNote = :authorsNote WHERE a.publicId = :publicId")
    void updateAuthorsNoteByPublicId(String authorsNote, UUID publicId);

    @Modifying
    @Query("UPDATE Adventure a SET a.contextAttributes.nudge = :nudge WHERE a.publicId = :publicId")
    void updateNudgeByPublicId(String nudge, UUID publicId);

    @Modifying
    @Query("UPDATE Adventure a SET a.contextAttributes.bump = :bump, a.contextAttributes.bumpFrequency = :bumpFrequency WHERE a.publicId = :publicId")
    void updateBumpByPublicId(String bump, int bumpFrequency, UUID publicId);
}
