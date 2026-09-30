package me.moirai.storyengine.core.port.inbound.adventure;

import java.time.Instant;
import java.util.UUID;

public record PendingAdventureInvitationDetails(
        UUID invitationId,
        UUID adventureId,
        String adventureName,
        String inviterDisplayName,
        Instant creationDate) {
}
