package me.moirai.storyengine.core.application.command.adventure;

import org.springframework.context.ApplicationEventPublisher;

import me.moirai.storyengine.common.annotation.Authorize;
import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.core.port.inbound.adventure.JoinAdventureWithCharacter;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.character.PlayerCharacterRepository;

@CommandHandler
@Authorize(operation = AuthorizationOperation.JOIN_ADVENTURE_WITH_CHARACTER, fields = { "#request.invitationId",
        "#request.playerCharacterId" })
public class JoinAdventureWithCharacterHandler extends AbstractCommandHandler<JoinAdventureWithCharacter, Void> {

    private final AdventureRepository adventureRepository;
    private final PlayerCharacterRepository playerCharacterRepository;
    private final ApplicationEventPublisher eventPublisher;

    public JoinAdventureWithCharacterHandler(
            AdventureRepository adventureRepository,
            PlayerCharacterRepository playerCharacterRepository,
            ApplicationEventPublisher eventPublisher) {

        this.adventureRepository = adventureRepository;
        this.playerCharacterRepository = playerCharacterRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Void execute(JoinAdventureWithCharacter command) {

        var adventure = adventureRepository.findByInvitationPublicId(command.invitationId())
                .orElseThrow(() -> new NotFoundException("Invitation not found"));

        var character = playerCharacterRepository.findByPublicId(command.playerCharacterId())
                .orElseThrow(() -> new NotFoundException("Player character not found"));

        character.validateHasClass();
        adventure.acceptInvitation(command.invitationId(), character.getId(), command.requesterId());
        adventureRepository.save(adventure);
        adventure.drainEvents().forEach(eventPublisher::publishEvent);

        return null;
    }
}
