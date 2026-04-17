package me.moirai.storyengine.core.application.command.message;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.core.port.inbound.message.DeleteMessage;
import me.moirai.storyengine.core.port.outbound.message.MessageRepository;

@CommandHandler
public class DeleteMessageHandler extends AbstractCommandHandler<DeleteMessage, Void> {

    private final MessageRepository messageRepository;

    public DeleteMessageHandler(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    public void validate(DeleteMessage command) {
        if (command.adventureId() == null) {
            throw new IllegalArgumentException("Adventure ID cannot be null");
        }

        if (command.messageId() == null) {
            throw new IllegalArgumentException("Message ID cannot be null");
        }
    }

    @Override
    public Void execute(DeleteMessage command) {
        messageRepository.deleteByPublicId(command.adventureId(), command.messageId());
        return null;
    }
}
