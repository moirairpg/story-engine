package me.moirai.storyengine.infrastructure.outbound.adapter.message;

import java.util.List;
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
}
