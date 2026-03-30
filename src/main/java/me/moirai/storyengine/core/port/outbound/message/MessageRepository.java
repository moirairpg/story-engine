package me.moirai.storyengine.core.port.outbound.message;

import java.util.List;
import java.util.UUID;

import me.moirai.storyengine.core.domain.message.Message;

public interface MessageRepository {
    Message save(Message message);
    void markAsChronicled(List<UUID> publicIds);
}
