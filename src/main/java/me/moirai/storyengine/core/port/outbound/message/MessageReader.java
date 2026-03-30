package me.moirai.storyengine.core.port.outbound.message;

import java.util.List;

public interface MessageReader {
    List<MessageData> findActiveByAdventureId(Long adventureId, int limit);
    List<MessageData> getAllActiveByAdventureId(Long adventureId);
}
