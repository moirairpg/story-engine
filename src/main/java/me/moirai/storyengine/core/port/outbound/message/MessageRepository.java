package me.moirai.storyengine.core.port.outbound.message;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import me.moirai.storyengine.core.domain.message.Message;

public interface MessageRepository {

    Message save(Message message);

    List<Message> saveAll(List<Message> messagesToChronicle);

    Optional<Message> getLastActive(Long adventureId);

    void deleteLastAssistantMessage(Long adventureId);

    void deleteByPublicId(UUID adventurePublicId, UUID messagePublicId);

    void updateContent(UUID adventurePublicId, UUID messagePublicId, String content);

    void deleteNewerThanByPublicId(UUID adventurePublicId, UUID messagePublicId);

    void deleteAllByAdventureId(Long adventureId);

    List<Message> findAllActiveByAdventureId(Long adventureId);

    List<Message> findLatestChronicledByAdventureId(Long adventureId, int limit);
}
