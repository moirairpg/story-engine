package me.moirai.storyengine.core.application.command.message;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.port.inbound.message.DeleteMessagesByAdventure;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.message.MessageRepository;

@CommandHandler
public class DeleteMessagesByAdventureHandler
        extends AbstractCommandHandler<DeleteMessagesByAdventure, Void> {

    private static final String ADVENTURE_NOT_FOUND = "Adventure was not found";
    private static final String ID_REQUIRED = "Adventure ID cannot be null";

    private final MessageRepository messageRepository;
    private final AdventureRepository adventureRepository;

    public DeleteMessagesByAdventureHandler(
            MessageRepository messageRepository,
            AdventureRepository adventureRepository) {

        this.messageRepository = messageRepository;
        this.adventureRepository = adventureRepository;
    }

    @Override
    public void validate(DeleteMessagesByAdventure command) {

        if (command.adventurePublicId() == null) {
            throw new IllegalArgumentException(ID_REQUIRED);
        }
    }

    @Override
    public Void execute(DeleteMessagesByAdventure command) {

        var adventure = adventureRepository.findByPublicId(command.adventurePublicId())
                .orElseThrow(() -> new NotFoundException(ADVENTURE_NOT_FOUND));

        messageRepository.deleteAllByAdventurePublicId(adventure.getPublicId());

        return null;
    }
}
