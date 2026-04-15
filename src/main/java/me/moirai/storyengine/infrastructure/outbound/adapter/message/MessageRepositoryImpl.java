package me.moirai.storyengine.infrastructure.outbound.adapter.message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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

    @Override
    public Optional<Message> getLastActive(Long adventureId) {
        return jpaRepository.findLastActive(adventureId);
    }

    @Override
    public List<Message> findActiveByAdventureId(Long adventureId, int limit) {
        var results = new ArrayList<>(jpaRepository.findWindowActive(adventureId, limit));
        Collections.reverse(results);
        return Collections.unmodifiableList(results);
    }

    @Override
    public void deleteLastAssistantMessage(Long adventureId) {
        jpaRepository.deleteLastAssistantMessage(adventureId);
    }

    @Override
    public void deleteByPublicId(UUID adventurePublicId, UUID messagePublicId) {
        jpaRepository.deleteByPublicId(adventurePublicId, messagePublicId);
    }

    @Override
    public void updateContent(UUID adventurePublicId, UUID messagePublicId, String content) {
        jpaRepository.updateContent(adventurePublicId, messagePublicId, content);
    }

    @Override
    public void deleteNewerThanByPublicId(UUID adventurePublicId, UUID messagePublicId) {
        jpaRepository.deleteNewerThanByPublicId(adventurePublicId, messagePublicId);
    }

    @Override
    public void deleteAllByAdventurePublicId(UUID adventurePublicId) {
        jpaRepository.deleteAllByAdventurePublicId(adventurePublicId);
    }
}
