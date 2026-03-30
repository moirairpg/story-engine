package me.moirai.storyengine.infrastructure.outbound.adapter.message;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import me.moirai.storyengine.core.domain.message.Message;
import me.moirai.storyengine.core.port.outbound.message.MessageRepository;

@Repository
public class MessageRepositoryImpl implements MessageRepository {

    private final MessageJpaRepository jpaRepository;

    public MessageRepositoryImpl(MessageJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Message save(Message message) {
        return jpaRepository.save(message);
    }

    @Override
    public void markAsChronicled(List<UUID> publicIds) {
        jpaRepository.markAsChronicled(publicIds);
    }
}
