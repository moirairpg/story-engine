package me.moirai.storyengine.core.application.command.character;

import org.springframework.context.ApplicationEventPublisher;

import me.moirai.storyengine.common.annotation.Authorize;
import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.core.port.inbound.character.DeletePlayerCharacter;
import me.moirai.storyengine.core.port.outbound.character.PlayerCharacterRepository;

// TODO: the event itself cannot go — PlayerCharacterDeletedEvent also drives
// AdventureDomainEventListener.onPlayerCharacterDeleted, which withdraws the character from every
// roster it belongs to. Only the other consumer, PlayerCharacterDeletedCleanupListener (image and
// vector, after commit), is the part under review as possible bloat. Removing it means doing both
// deletes inline again and accepting that a rollback destroys data the database still considers live.
@CommandHandler
@Authorize(operation = AuthorizationOperation.DELETE_PLAYER_CHARACTER, fields = "#request.characterId")
public class DeletePlayerCharacterHandler extends AbstractCommandHandler<DeletePlayerCharacter, Void> {

    private final PlayerCharacterRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    public DeletePlayerCharacterHandler(
            PlayerCharacterRepository repository,
            ApplicationEventPublisher eventPublisher) {

        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Void execute(DeletePlayerCharacter command) {

        var character = repository.findByPublicId(command.characterId())
                .orElseThrow(() -> new NotFoundException("Player character not found"));

        character.communicateCharacterDeleted();
        character.drainEvents().forEach(eventPublisher::publishEvent);

        repository.deleteByPublicId(command.characterId());

        return null;
    }
}
