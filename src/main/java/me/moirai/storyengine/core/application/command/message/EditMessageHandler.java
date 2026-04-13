package me.moirai.storyengine.core.application.command.message;

import static me.moirai.storyengine.common.util.DefaultStringProcessors.addChatPrefix;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.port.inbound.message.EditMessage;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.message.MessageRepository;

@CommandHandler
public class EditMessageHandler extends AbstractCommandHandler<EditMessage, Void> {

    private final AdventureRepository adventureRepository;
    private final MessageRepository messageRepository;

    public EditMessageHandler(AdventureRepository adventureRepository, MessageRepository messageRepository) {
        this.adventureRepository = adventureRepository;
        this.messageRepository = messageRepository;
    }

    @Override
    public void validate(EditMessage command) {
        if (command.adventureId() == null) {
            throw new IllegalArgumentException("Adventure ID cannot be null");
        }

        if (command.messageId() == null) {
            throw new IllegalArgumentException("Message ID cannot be null");
        }

        if (command.content() == null || command.content().isBlank()) {
            throw new IllegalArgumentException("Content cannot be blank");
        }
    }

    @Override
    public Void execute(EditMessage command) {
        var characterName = adventureRepository.findByPublicId(command.adventureId())
                .orElseThrow(() -> new NotFoundException("Adventure not found"))
                .getLorebookEntryByPlayerId(command.username())
                .map(e -> e.getName())
                .orElse(command.username());

        var prefixedContent = addChatPrefix(characterName).apply(command.content());

        messageRepository.updateContent(command.adventureId(), command.messageId(), prefixedContent);
        messageRepository.deleteNewerThanByPublicId(command.adventureId(), command.messageId());
        return null;
    }
}
