package me.moirai.storyengine.infrastructure.outbound.adapter.message;

import org.springframework.data.jpa.repository.JpaRepository;

import me.moirai.storyengine.core.domain.message.Message;

public interface MessageJpaRepository extends JpaRepository<Message, Long> {
}
