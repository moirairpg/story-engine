package me.moirai.storyengine.core.application.command.world;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.domain.Permissions;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.port.inbound.world.CreateWorld;
import me.moirai.storyengine.core.port.inbound.world.WorldDetails;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

@CommandHandler
public class CreateWorldHandler extends AbstractCommandHandler<CreateWorld, WorldDetails> {

    private final WorldRepository repository;

    public CreateWorldHandler(WorldRepository repository) {

        this.repository = repository;
    }

    @Override
    public WorldDetails execute(CreateWorld command) {

        var permissions = Permissions.builder()
                .ownerId(command.requesterId())
                .usersAllowedToRead(command.usersAllowedToRead())
                .usersAllowedToWrite(command.usersAllowedToWrite())
                .build();

        var world = repository.save(World.builder()
                .name(command.name())
                .description(command.description())
                .adventureStart(command.adventureStart())
                .visibility(command.visibility())
                .permissions(permissions)
                .creatorId(command.requesterId())
                .build());

        command.lorebookEntries().forEach(entry -> world.addLorebookEntry(
                entry.name(),
                entry.regex(),
                entry.description()));

        repository.save(world);

        return mapResult(repository.save(world));
    }

    private WorldDetails mapResult(World world) {

        return new WorldDetails(
                world.getPublicId(),
                world.getName(),
                world.getDescription(),
                world.getAdventureStart(),
                world.getVisibility().name(),
                world.getOwnerId(),
                world.getUsersAllowedToRead(),
                world.getUsersAllowedToWrite(),
                world.getCreationDate(),
                world.getLastUpdateDate());
    }
}
