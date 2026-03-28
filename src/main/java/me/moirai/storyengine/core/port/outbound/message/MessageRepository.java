package me.moirai.storyengine.core.port.outbound.message;

import me.moirai.storyengine.core.domain.message.Message;

public interface MessageRepository {
    Message save(Message message);
}
