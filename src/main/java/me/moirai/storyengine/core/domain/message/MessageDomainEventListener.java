package me.moirai.storyengine.core.domain.message;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import me.moirai.storyengine.core.domain.adventure.AdventureDeletedEvent;
import me.moirai.storyengine.core.port.outbound.message.MessageRepository;

@Component
public class MessageDomainEventListener {

    private final MessageRepository messageRepository;

    public MessageDomainEventListener(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onAdventureDeleted(AdventureDeletedEvent event) {

        messageRepository.deleteAllByAdventureId(event.getAdventureId());
    }
}
