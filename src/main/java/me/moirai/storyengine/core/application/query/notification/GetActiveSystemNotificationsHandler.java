package me.moirai.storyengine.core.application.query.notification;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import me.moirai.storyengine.common.annotation.QueryHandler;
import me.moirai.storyengine.common.cqs.query.AbstractQueryHandler;
import me.moirai.storyengine.common.enums.NotificationKind;
import me.moirai.storyengine.common.enums.NotificationLevel;
import me.moirai.storyengine.common.enums.NotificationType;
import me.moirai.storyengine.core.port.inbound.notification.GetActiveSystemNotifications;
import me.moirai.storyengine.core.port.inbound.notification.NotificationDetails;
import me.moirai.storyengine.core.port.outbound.adventure.InvitationReader;
import me.moirai.storyengine.core.port.outbound.adventure.PendingInvitationRow;
import me.moirai.storyengine.core.port.outbound.notification.ActiveSystemNotificationReader;

@QueryHandler
public class GetActiveSystemNotificationsHandler
        extends AbstractQueryHandler<GetActiveSystemNotifications, List<NotificationDetails>> {

    private final ActiveSystemNotificationReader reader;
    private final InvitationReader invitationReader;

    public GetActiveSystemNotificationsHandler(
            ActiveSystemNotificationReader reader,
            InvitationReader invitationReader) {

        this.reader = reader;
        this.invitationReader = invitationReader;
    }

    @Override
    public List<NotificationDetails> execute(GetActiveSystemNotifications query) {

        var notifications = reader.getActiveUnreadSystemNotifications(query.username());

        var invitations = invitationReader.getAllPendingByRecipient(query.username()).stream()
                .map(this::toNotificationDetails)
                .toList();

        return Stream.concat(notifications.stream(), invitations.stream())
                .sorted(Comparator.comparing(NotificationDetails::creationDate).reversed())
                .toList();
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
