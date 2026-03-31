package me.moirai.storyengine.infrastructure.outbound.adapter.message;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import me.moirai.storyengine.core.domain.message.Message;

public interface MessageJpaRepository extends JpaRepository<Message, Long> {

    @Modifying
    @Query("""
            UPDATE Message m
               SET m.status = me.moirai.storyengine.core.domain.message.MessageStatus.CHRONICLED
             WHERE m.publicId IN :publicIds
            """)
    void markAsChronicled(@Param("publicIds") List<UUID> publicIds);

    @Query("""
            SELECT m FROM Message m
             WHERE m.adventureId = :adventureId
               AND m.status = me.moirai.storyengine.core.domain.message.MessageStatus.ACTIVE
             ORDER BY m.creationDate DESC
             LIMIT 1
            """)
    Optional<Message> findLastActive(@Param("adventureId") Long adventureId);

    @Query("""
            SELECT m FROM Message m
             WHERE m.adventureId = :adventureId
               AND m.status = me.moirai.storyengine.core.domain.message.MessageStatus.ACTIVE
             ORDER BY m.creationDate DESC
             LIMIT :limit
            """)
    List<Message> findWindowActive(@Param("adventureId") Long adventureId, @Param("limit") int limit);

    @Modifying
    @Query("""
            DELETE FROM Message m
             WHERE m.id = (
                   SELECT MAX(m2.id)
                     FROM Message m2
                    WHERE m2.adventureId = :adventureId
                      AND m2.role = me.moirai.storyengine.common.enums.AiRole.ASSISTANT
                      AND m2.status = me.moirai.storyengine.core.domain.message.MessageStatus.ACTIVE
                   )
            """)
    void deleteLastAssistantMessage(@Param("adventureId") Long adventureId);
}
