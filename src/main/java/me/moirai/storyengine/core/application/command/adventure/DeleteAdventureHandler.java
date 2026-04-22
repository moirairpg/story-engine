package me.moirai.storyengine.core.application.command.adventure;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.port.inbound.adventure.DeleteAdventure;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.chronicle.ChronicleSegmentRepository;
import me.moirai.storyengine.core.port.outbound.message.MessageRepository;
import me.moirai.storyengine.core.port.outbound.notification.NotificationRepository;
import me.moirai.storyengine.core.port.outbound.storage.StoragePort;
import me.moirai.storyengine.core.port.outbound.vectorsearch.ChronicleVectorSearchPort;
import me.moirai.storyengine.core.port.outbound.vectorsearch.LorebookVectorSearchPort;

@CommandHandler
public class DeleteAdventureHandler extends AbstractCommandHandler<DeleteAdventure, Void> {

    private static final String ADVENTURE_NOT_FOUND = "Adventure to be deleted was not found";
    private static final String ID_CANNOT_BE_NULL_OR_EMPTY = "Adventure ID cannot be null or empty";

    private final AdventureRepository repository;
    private final MessageRepository messageRepository;
    private final ChronicleSegmentRepository chronicleSegmentRepository;
    private final LorebookVectorSearchPort lorebookVectorSearchPort;
    private final ChronicleVectorSearchPort chronicleVectorSearchPort;
    private final StoragePort storagePort;
    private final NotificationRepository notificationRepository;

    public DeleteAdventureHandler(
            AdventureRepository repository,
            MessageRepository messageRepository,
            ChronicleSegmentRepository chronicleSegmentRepository,
            LorebookVectorSearchPort lorebookVectorSearchPort,
            ChronicleVectorSearchPort chronicleVectorSearchPort,
            StoragePort storagePort,
            NotificationRepository notificationRepository) {

        this.repository = repository;
        this.messageRepository = messageRepository;
        this.chronicleSegmentRepository = chronicleSegmentRepository;
        this.lorebookVectorSearchPort = lorebookVectorSearchPort;
        this.chronicleVectorSearchPort = chronicleVectorSearchPort;
        this.storagePort = storagePort;
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void validate(DeleteAdventure command) {

        if (command.adventureId() == null) {
            throw new IllegalArgumentException(ID_CANNOT_BE_NULL_OR_EMPTY);
        }
    }

    @Override
    public Void execute(DeleteAdventure command) {

        var adventure = repository.findByPublicId(command.adventureId())
                .orElseThrow(() -> new NotFoundException(ADVENTURE_NOT_FOUND));

        if (adventure.getImageKey() != null) {
            storagePort.delete(adventure.getImageKey());
        }

        messageRepository.deleteAllByAdventurePublicId(command.adventureId());
        chronicleSegmentRepository.deleteAllByAdventurePublicId(command.adventureId());
        lorebookVectorSearchPort.deleteAllByAdventureId(command.adventureId());
        chronicleVectorSearchPort.deleteAllByAdventureId(command.adventureId());
        notificationRepository.deleteAllGameNotificationsByAdventureId(adventure.getId());
        repository.deleteByPublicId(command.adventureId());

        return null;
    }
}
