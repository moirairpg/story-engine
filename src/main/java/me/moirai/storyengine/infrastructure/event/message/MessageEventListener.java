package me.moirai.storyengine.infrastructure.event.message;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import me.moirai.storyengine.common.cqs.command.CommandRunner;
import me.moirai.storyengine.core.domain.adventure.AdventureDeletedEvent;
import me.moirai.storyengine.core.port.inbound.notification.DeleteNotificationsByAdventure;

@Component
public class MessageEventListener {

    private final CommandRunner commandRunner;

    public MessageEventListener(CommandRunner commandRunner) {

        this.commandRunner = commandRunner;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onAdventureDeleted(AdventureDeletedEvent event) {

        commandRunner.run(new DeleteNotificationsByAdventure(event.getAdventureId()));
    }
}
