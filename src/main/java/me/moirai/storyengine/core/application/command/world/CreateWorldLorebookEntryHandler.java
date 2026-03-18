package me.moirai.storyengine.core.application.command.world;

import io.micrometer.common.util.StringUtils;
import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.core.domain.world.World;
import me.moirai.storyengine.core.domain.world.WorldLorebookEntry;
import me.moirai.storyengine.core.port.inbound.world.CreateWorldLorebookEntry;
import me.moirai.storyengine.core.port.inbound.world.WorldLorebookEntryDetails;
import me.moirai.storyengine.core.port.outbound.world.WorldRepository;

@CommandHandler
public class CreateWorldLorebookEntryHandler
        extends AbstractCommandHandler<CreateWorldLorebookEntry, WorldLorebookEntryDetails> {

    private static final String WORLD_TO_BE_UPDATED_WAS_NOT_FOUND = "World to be updated was not found";

    private final WorldRepository repository;

    public CreateWorldLorebookEntryHandler(WorldRepository repository) {

        this.repository = repository;
    }

    @Override
    public void validate(CreateWorldLorebookEntry command) {

        if (command.worldId() == null) {
            throw new IllegalArgumentException("World ID cannot be null");
        }

        if (StringUtils.isBlank(command.name())) {
            throw new IllegalArgumentException("Name cannot be null");
        }

        if (StringUtils.isBlank(command.description())) {
            throw new IllegalArgumentException("Description cannot be null");
        }
    }

    @Override
    public WorldLorebookEntryDetails execute(CreateWorldLorebookEntry command) {

        var world = repository.findByPublicId(command.worldId())
                .orElseThrow(() -> new AssetNotFoundException(WORLD_TO_BE_UPDATED_WAS_NOT_FOUND));

        var entry = world.addLorebookEntry(
                command.name(),
                command.regex(),
                command.description());

        repository.save(world);
        return mapResult(world, entry);
    }

    private WorldLorebookEntryDetails mapResult(World world, WorldLorebookEntry entry) {

        return new WorldLorebookEntryDetails(
                entry.getPublicId(),
                world.getPublicId(),
                entry.getName(),
                entry.getRegex(),
                entry.getDescription(),
                entry.getCreationDate(),
                entry.getLastUpdateDate());
    }
}
