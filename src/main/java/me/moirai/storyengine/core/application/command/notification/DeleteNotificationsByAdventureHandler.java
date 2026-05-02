package me.moirai.storyengine.core.application.command.notification;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.port.inbound.notification.DeleteNotificationsByAdventure;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.notification.NotificationRepository;

@CommandHandler
public class DeleteNotificationsByAdventureHandler
        extends AbstractCommandHandler<DeleteNotificationsByAdventure, Void> {

    private static final String ADVENTURE_NOT_FOUND = "Adventure was not found";
    private static final String ID_REQUIRED = "Adventure ID cannot be null";

    private final NotificationRepository notificationRepository;
    private final AdventureRepository adventureRepository;

    public DeleteNotificationsByAdventureHandler(
            NotificationRepository notificationRepository,
            AdventureRepository adventureRepository) {

        this.notificationRepository = notificationRepository;
        this.adventureRepository = adventureRepository;
    }

    @Override
    public void validate(DeleteNotificationsByAdventure command) {

        if (command.adventurePublicId() == null) {
            throw new IllegalArgumentException(ID_REQUIRED);
        }
    }

    @Override
    public Void execute(DeleteNotificationsByAdventure command) {

        var adventure = adventureRepository.findByPublicId(command.adventurePublicId())
                .orElseThrow(() -> new NotFoundException(ADVENTURE_NOT_FOUND));

        notificationRepository.deleteAllGameNotificationsByAdventureId(adventure.getId());

        return null;
    }
}
