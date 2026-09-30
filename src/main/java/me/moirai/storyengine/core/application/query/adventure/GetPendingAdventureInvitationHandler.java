package me.moirai.storyengine.core.application.query.adventure;

import me.moirai.storyengine.common.annotation.QueryHandler;
import me.moirai.storyengine.common.cqs.query.AbstractQueryHandler;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.core.port.inbound.adventure.GetPendingAdventureInvitation;
import me.moirai.storyengine.core.port.inbound.adventure.PendingAdventureInvitationDetails;
import me.moirai.storyengine.core.port.outbound.adventure.InvitationReader;

@QueryHandler
public class GetPendingAdventureInvitationHandler
        extends AbstractQueryHandler<GetPendingAdventureInvitation, PendingAdventureInvitationDetails> {

    private final InvitationReader invitationReader;

    public GetPendingAdventureInvitationHandler(InvitationReader invitationReader) {
        this.invitationReader = invitationReader;
    }

    @Override
    public PendingAdventureInvitationDetails execute(GetPendingAdventureInvitation query) {

        return invitationReader
                .getPendingByAdventureAndRecipient(query.adventureId(), query.requesterUsername())
                .map(row -> new PendingAdventureInvitationDetails(
                        row.invitationId(),
                        row.adventureId(),
                        row.adventureName(),
                        row.inviterDisplayName(),
                        row.creationDate()))
                .orElseThrow(() -> new NotFoundException("No pending invitation for this adventure"));
    }
}
