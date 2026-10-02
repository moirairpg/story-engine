package me.moirai.storyengine.core.application.command.adventure;

import org.springframework.context.ApplicationEventPublisher;

import me.moirai.storyengine.common.annotation.Authorize;
import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.core.port.inbound.adventure.DeclineAdventureInvitation;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;

@CommandHandler
@Authorize(operation = AuthorizationOperation.DECLINE_ADVENTURE_INVITATION, fields = "#request.invitationId")
public class DeclineAdventureInvitationHandler extends AbstractCommandHandler<DeclineAdventureInvitation, Void> {

    private final AdventureRepository adventureRepository;
    private final ApplicationEventPublisher eventPublisher;

    public DeclineAdventureInvitationHandler(
            AdventureRepository adventureRepository,
            ApplicationEventPublisher eventPublisher) {

        this.adventureRepository = adventureRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Void execute(DeclineAdventureInvitation command) {

        var adventure = adventureRepository.findByInvitationPublicId(command.invitationId())
                .orElseThrow(() -> new NotFoundException("Invitation not found"));

        adventure.declineInvitation(command.invitationId());
        adventureRepository.save(adventure);
        adventure.drainEvents().forEach(eventPublisher::publishEvent);

        return null;
    }
}
