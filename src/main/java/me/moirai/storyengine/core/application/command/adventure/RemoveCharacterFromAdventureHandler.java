package me.moirai.storyengine.core.application.command.adventure;

import org.springframework.context.ApplicationEventPublisher;

import me.moirai.storyengine.common.annotation.Authorize;
import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.core.port.inbound.adventure.RemoveCharacterFromAdventure;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;
import me.moirai.storyengine.core.port.outbound.character.PlayerCharacterRepository;

@CommandHandler
@Authorize(operation = AuthorizationOperation.REMOVE_CHARACTER_FROM_ADVENTURE, fields = { "#request.adventureId",
        "#request.playerCharacterId" })
public class RemoveCharacterFromAdventureHandler extends AbstractCommandHandler<RemoveCharacterFromAdventure, Void> {

    private final AdventureRepository adventureRepository;
    private final PlayerCharacterRepository playerCharacterRepository;
    private final ApplicationEventPublisher eventPublisher;

    public RemoveCharacterFromAdventureHandler(
            AdventureRepository adventureRepository,
            PlayerCharacterRepository playerCharacterRepository,
            ApplicationEventPublisher eventPublisher) {

        this.adventureRepository = adventureRepository;
        this.playerCharacterRepository = playerCharacterRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Void execute(RemoveCharacterFromAdventure command) {

        var adventure = adventureRepository.findByPublicId(command.adventureId())
                .orElseThrow(() -> new NotFoundException("Adventure not found"));

        var character = playerCharacterRepository.findByPublicId(command.playerCharacterId())
                .orElseThrow(() -> new NotFoundException("Player character not found"));

        if (character.getPlayerId().equals(command.requesterId())) {
            adventure.leave(character.getId());
        } else {
            adventure.expel(character.getId());
        }

        adventureRepository.save(adventure);
        adventure.drainEvents().forEach(eventPublisher::publishEvent);

        return null;
    }
}
