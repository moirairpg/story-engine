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

    Optional<Adventure> findByChannelId(String channelId);

    Optional<Adventure> findByPublicId(UUID publicId);

    void deleteByPublicId(UUID publicId);

    @Query("SELECT cc.gameMode FROM Adventure cc WHERE cc.channelId = :channelId")
    String getGameModeByChannelId(String channelId);

    @Modifying
    @Query("UPDATE Adventure a SET a.contextAttributes.remember = :remember WHERE a.channelId = :channelId")
    void updateRememberByChannelId(String remember, String channelId);

    @Modifying
    @Query("UPDATE Adventure a SET a.contextAttributes.authorsNote = :authorsNote WHERE a.channelId = :channelId")
    void updateAuthorsNoteByChannelId(String authorsNote, String channelId);

    @Modifying
    @Query("UPDATE Adventure a SET a.contextAttributes.nudge = :nudge WHERE a.channelId = :channelId")
    void updateNudgeByChannelId(String nudge, String channelId);

    @Modifying
    @Query("UPDATE Adventure a SET a.contextAttributes.bump = :bump, a.contextAttributes.bumpFrequency = :bumpFrequency WHERE a.channelId = :channelId")
    void updateBumpByChannelId(String bump, int bumpFrequency, String channelId);
}
