package me.moirai.storyengine.infrastructure.outbound.adapter.message;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import me.moirai.storyengine.core.domain.message.Message;

public interface MessageJpaRepository extends JpaRepository<Message, Long> {

    @Query("""
            SELECT m FROM Message m
             WHERE m.adventureId = :adventureId
               AND m.status = me.moirai.storyengine.core.domain.message.MessageStatus.ACTIVE
             ORDER BY m.creationDate DESC
             LIMIT 1
            """)
    Optional<Message> findLastActive(Long adventureId);

    @Query("""
            SELECT m FROM Message m
             WHERE m.adventureId = :adventureId
               AND m.status = me.moirai.storyengine.core.domain.message.MessageStatus.ACTIVE
             ORDER BY m.creationDate DESC
             LIMIT :limit
            """)
    List<Message> findWindowActive(Long adventureId, int limit);

    @Modifying
    @Query("""
            DELETE FROM Message m
             WHERE m.id = (
                   SELECT MAX(m2.id)
                     FROM Message m2
                    WHERE m2.adventureId = :adventureId
                      AND m2.role = me.moirai.storyengine.common.enums.MessageAuthorRole.ASSISTANT
                      AND m2.status = me.moirai.storyengine.core.domain.message.MessageStatus.ACTIVE
                   )
            """)
    void deleteLastAssistantMessage(Long adventureId);

    @Modifying
    @Query("""
            DELETE FROM Message m
             WHERE m.publicId = :messagePublicId
               AND m.adventureId = (
                   SELECT a.id FROM Adventure a WHERE a.publicId = :adventurePublicId
               )
            """)
    void deleteByPublicId(UUID adventurePublicId, UUID messagePublicId);

    @Modifying
    @Query("""
            UPDATE Message m
               SET m.content = :content
             WHERE m.publicId = :messagePublicId
               AND m.adventureId = (
                   SELECT a.id FROM Adventure a WHERE a.publicId = :adventurePublicId
               )
            """)
    void updateContent(UUID adventurePublicId, UUID messagePublicId, String content);

    @Modifying
    @Query("""
            DELETE FROM Message m
             WHERE m.adventureId = (
                       SELECT a.id FROM Adventure a WHERE a.publicId = :adventurePublicId
                   )
               AND m.id > (
                       SELECT m2.id FROM Message m2 WHERE m2.publicId = :messagePublicId
                   )
            """)
    void deleteNewerThanByPublicId(UUID adventurePublicId, UUID messagePublicId);

    @Modifying
    @Query("""
            DELETE FROM Message m
             WHERE m.adventureId = (
                   SELECT a.id FROM Adventure a WHERE a.publicId = :adventurePublicId
                   )
            """)
    void deleteAllByAdventurePublicId(UUID adventurePublicId);

    @Query("""
            SELECT m FROM Message m
             WHERE m.adventureId = (
                   SELECT a.id FROM Adventure a WHERE a.publicId = :adventurePublicId
                   )
               AND m.status = me.moirai.storyengine.core.domain.message.MessageStatus.ACTIVE
             ORDER BY m.creationDate ASC
            """)
    List<Message> findAllActiveByAdventurePublicId(UUID adventurePublicId);
}
