package me.moirai.storyengine.core.port.outbound.adventure;

import java.time.Instant;
import java.util.UUID;

public record PendingInvitationRow(
        UUID invitationId,
        UUID adventureId,
        String adventureName,
        String inviterDisplayName,
        String recipientUsername,
        Instant creationDate) {
}
