package me.moirai.storyengine.core.application.command.world;

import org.springframework.context.ApplicationEventPublisher;

import me.moirai.storyengine.common.annotation.Authorize;
import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.exception.NotFoundException;
import me.moirai.storyengine.common.security.authorization.AuthorizationOperation;
import me.moirai.storyengine.core.port.inbound.world.DeleteWorld;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

// TODO: candidate for removal. WorldDeletedEvent has exactly one consumer —
// WorldDomainEventListener.onWorldDeleted, which deletes the image from storage after commit. Nothing
// else reacts to it. Inlining storagePort.delete here again would drop the event, the listener method
// and this indirection; the cost is that a rollback after the delete would destroy an image the
// database still considers live.
@CommandHandler
@Authorize(operation = AuthorizationOperation.DELETE_WORLD, fields = "#request.worldId")
public class DeleteWorldHandler extends AbstractCommandHandler<DeleteWorld, Void> {

    private static final String WORLD_TO_BE_VIEWED_WAS_NOT_FOUND = "World to be viewed was not found";
    private static final String ID_CANNOT_BE_NULL_OR_EMPTY = "World ID cannot be null or empty";

    private final WorldRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    public DeleteWorldHandler(
            WorldRepository repository,
            ApplicationEventPublisher eventPublisher) {

        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void validate(DeleteWorld command) {

        if (command.worldId() == null) {
            throw new IllegalArgumentException(ID_CANNOT_BE_NULL_OR_EMPTY);
        }
    }

    @Override
    public Void execute(DeleteWorld command) {

        var world = repository.findByPublicId(command.worldId())
                .orElseThrow(() -> new NotFoundException(WORLD_TO_BE_VIEWED_WAS_NOT_FOUND));

        world.communicateWorldDeleted();
        world.drainEvents().forEach(eventPublisher::publishEvent);

        repository.deleteByPublicId(command.worldId());

        return null;
    }
}
