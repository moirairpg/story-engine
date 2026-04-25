package me.moirai.storyengine.infrastructure.event.adventure;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import me.moirai.storyengine.common.cqs.command.CommandRunner;
import me.moirai.storyengine.core.application.event.adventure.AdventureMessageWindowOverflowedEvent;
import me.moirai.storyengine.core.port.inbound.chronicle.UpdateChronicle;

@Component
public class ChronicleUpdateListener {

    private final CommandRunner commandRunner;

    public ChronicleUpdateListener(CommandRunner commandRunner) {
        this.commandRunner = commandRunner;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAdventureMessageSaved(AdventureMessageWindowOverflowedEvent event) {
        commandRunner.run(new UpdateChronicle(event.adventurePublicId()));
    }
}
