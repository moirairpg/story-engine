package me.moirai.storyengine.core.application.event.adventure;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import me.moirai.storyengine.common.enums.NotificationKind;
import me.moirai.storyengine.common.enums.NotificationLevel;
import me.moirai.storyengine.common.enums.NotificationType;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.application.event.notification.NotificationCreatedEvent;
import me.moirai.storyengine.core.domain.adventure.EnrolledCharacterDeletedEvent;
import me.moirai.storyengine.core.domain.adventure.PlayerExpelledFromAdventureEvent;
import me.moirai.storyengine.core.domain.adventure.PlayerLeftAdventureEvent;
import me.moirai.storyengine.core.domain.notification.Notification;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.notification.NotificationRepository;
import me.moirai.storyengine.core.port.outbound.userdetails.UserRepository;

@Component
public class AdventureRosterEventListener {

    private static final String USER_NOT_FOUND = "User not found";

    private final NotificationRepository notificationRepository;
    private final AdventureRepository adventureRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public AdventureRosterEventListener(
            NotificationRepository notificationRepository,
            AdventureRepository adventureRepository,
            UserRepository userRepository,
            ApplicationEventPublisher eventPublisher) {

        this.notificationRepository = notificationRepository;
        this.adventureRepository = adventureRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    @EventListener
    public void onPlayerLeft(PlayerLeftAdventureEvent event) {

        var displayName = resolveDisplayName(event.getPlayerId());

        publish(notifyManagers(
                event.getAdventureId(),
                event.getAdventurePublicId(),
                displayName + " left " + event.getAdventureName(),
                NotificationKind.ADVENTURE_MEMBER_LEFT));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    @EventListener
    public void onPlayerExpelled(PlayerExpelledFromAdventureEvent event) {

        publish(Notification.builder()
                .type(NotificationType.SYSTEM)
                .level(NotificationLevel.INFO)
                .message("You were removed from " + event.getAdventureName())
                .adventureId(event.getAdventureId())
                .isInteractable(false)
                .metadata(Map.<String, Object>of(
                        "kind", NotificationKind.ADVENTURE_MEMBER_REMOVED.name(),
                        "adventureId", event.getAdventurePublicId().toString()))
                .recipientUserIds(List.of(event.getPlayerId()))
                .build());
    }

    @Transactional(propagation = Propagation.MANDATORY)
    @EventListener
    public void onEnrolledCharacterDeleted(EnrolledCharacterDeletedEvent event) {

        var displayName = resolveDisplayName(event.getPlayerId());

        publish(notifyManagers(
                event.getAdventureId(),
                event.getAdventurePublicId(),
                displayName + " deleted their character in " + event.getAdventureName(),
                NotificationKind.ADVENTURE_CHARACTER_DELETED));
    }

    private String resolveDisplayName(Long playerId) {

        return userRepository.findById(playerId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND))
                .getDisplayName();
    }

    private Notification notifyManagers(
            Long adventureId,
            UUID adventurePublicId,
            String message,
            NotificationKind kind) {

        var managerUserIds = adventureRepository.findManagerUserIdsByAdventureId(adventureId);

        return Notification.builder()
                .type(NotificationType.SYSTEM)
                .level(NotificationLevel.INFO)
                .message(message)
                .adventureId(adventureId)
                .isInteractable(false)
                .metadata(Map.<String, Object>of(
                        "kind", kind.name(),
                        "adventureId", adventurePublicId.toString()))
                .recipientUserIds(managerUserIds)
                .build();
    }

    private void publish(Notification notification) {

        var saved = notificationRepository.save(notification);

        eventPublisher.publishEvent(new NotificationCreatedEvent(saved.getPublicId()));
    }
}
