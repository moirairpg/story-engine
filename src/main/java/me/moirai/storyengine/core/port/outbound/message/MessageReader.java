package me.moirai.storyengine.core.port.outbound.message;

import java.util.List;
import java.util.UUID;

public interface MessageReader {
    List<MessageData> getAllActiveByAdventureId(UUID adventurePublicId);
}
