package me.moirai.storyengine.core.application.command.world;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.domain.Permission;
import me.moirai.storyengine.common.enums.PermissionLevel;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.port.inbound.world.UpdateWorld;
import me.moirai.storyengine.core.port.inbound.world.WorldDetails;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

@CommandHandler
public class UpdateWorldHandler extends AbstractCommandHandler<UpdateWorld, WorldDetails> {

    private static final String ID_CANNOT_BE_NULL_OR_EMPTY = "World ID cannot be null or empty";
    private static final String WORLD_NOT_FOUND = "World to be updated was not found";

    private final WorldRepository repository;

    public UpdateWorldHandler(WorldRepository repository) {

        this.repository = repository;
    }

    @Override
    public void validate(UpdateWorld command) {

        if (command.worldId() == null) {
            throw new IllegalArgumentException(ID_CANNOT_BE_NULL_OR_EMPTY);
        }
    }

    @Override
    public WorldDetails execute(UpdateWorld command) {

        var world = repository.findByPublicId(command.worldId())
                .orElseThrow(() -> new AssetNotFoundException(WORLD_NOT_FOUND));

        world.updateName(command.name());
        world.updateDescription(command.description());
        world.updateAdventureStart(command.adventureStart());

        if (command.visibility() != null) {
            switch (command.visibility()) {
                case PUBLIC -> world.makePublic();
                case PRIVATE -> world.makePrivate();
                default -> world.makePrivate();
            }
        }

        emptyIfNull(command.usersAllowedToReadToAdd()).forEach(id -> world.grant(new Permission(id, PermissionLevel.READ)));
        emptyIfNull(command.usersAllowedToWriteToAdd()).forEach(id -> world.grant(new Permission(id, PermissionLevel.WRITE)));
        emptyIfNull(command.usersAllowedToReadToRemove()).forEach(world::revoke);
        emptyIfNull(command.usersAllowedToWriteToRemove()).forEach(world::revoke);

        return mapResult(repository.save(world));
    }

    private WorldDetails mapResult(World world) {

        return new WorldDetails(
                world.getPublicId(),
                world.getName(),
                world.getDescription(),
                world.getAdventureStart(),
                world.getVisibility().name(),
                world.getPermissions(),
                world.getCreationDate(),
                world.getLastUpdateDate());
    }
}
