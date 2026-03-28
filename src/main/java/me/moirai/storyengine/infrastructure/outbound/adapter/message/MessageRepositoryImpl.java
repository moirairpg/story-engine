package me.moirai.storyengine.infrastructure.outbound.adapter.message;

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
}
