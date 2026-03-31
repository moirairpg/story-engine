package me.moirai.storyengine.core.port.outbound.message;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import me.moirai.storyengine.core.domain.message.Message;

public interface MessageRepository {
    Message save(Message message);
    void markAsChronicled(List<UUID> publicIds);
    Optional<Message> getLastActive(Long adventureId);
    List<Message> findActiveByAdventureId(Long adventureId, int limit);
    void deleteLastAssistantMessage(Long adventureId);
}
