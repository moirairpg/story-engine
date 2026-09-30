package me.moirai.storyengine.infrastructure.event.adventure;

import java.util.List;
import java.util.Map;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import me.moirai.storyengine.core.domain.adventure.UserInvitedToAdventureEvent;
import me.moirai.storyengine.common.enums.NotificationKind;
import me.moirai.storyengine.common.enums.NotificationLevel;
import me.moirai.storyengine.common.enums.NotificationType;
import me.moirai.storyengine.core.port.inbound.notification.NotificationDetails;
import me.moirai.storyengine.core.port.outbound.adventure.InvitationReader;
import me.moirai.storyengine.core.port.outbound.adventure.PendingInvitationRow;

@Component
public class AdventureInvitationEventListener {

    private final InvitationReader invitationReader;
    private final SimpMessagingTemplate messagingTemplate;

    public AdventureInvitationEventListener(
            InvitationReader invitationReader,
            SimpMessagingTemplate messagingTemplate) {

        this.invitationReader = invitationReader;
        this.messagingTemplate = messagingTemplate;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserInvitedToAdventure(UserInvitedToAdventureEvent event) {

        invitationReader.getPendingByPublicId(event.getInvitationPublicId())
                .ifPresent(row -> messagingTemplate.convertAndSendToUser(
                        row.recipientUsername(),
                        "/queue/notifications/system",
                        toNotificationDetails(row)));
    }

    private NotificationDetails toNotificationDetails(PendingInvitationRow row) {

        return new NotificationDetails(
                row.invitationId(),
                row.inviterDisplayName() + " invited you to join " + row.adventureName(),
                NotificationType.SYSTEM,
                NotificationLevel.INFO,
                List.of(row.recipientUsername()),
                null,
                true,
                Map.of(
                        "kind", NotificationKind.ADVENTURE_INVITE.name(),
                        "adventureName", row.adventureName()),
                row.creationDate(),
                row.creationDate());
    }
}
