package me.moirai.storyengine.core.application.event;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import me.moirai.storyengine.common.cqs.command.CommandRunner;
import me.moirai.storyengine.core.domain.chronicle.MessageWindowOverflowEvent;
import me.moirai.storyengine.core.port.inbound.chronicle.UpdateChronicle;

@Component
public class ChronicleUpdateListener {

    private final CommandRunner commandRunner;

    public ChronicleUpdateListener(CommandRunner commandRunner) {
        this.commandRunner = commandRunner;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAdventureMessageSaved(MessageWindowOverflowEvent event) {
        commandRunner.run(new UpdateChronicle(event.adventurePublicId()));
    }
}
