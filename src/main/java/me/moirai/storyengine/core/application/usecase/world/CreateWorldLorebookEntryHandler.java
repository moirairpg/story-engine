package me.moirai.storyengine.core.application.usecase.world;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.port.inbound.world.CreateWorldLorebookEntry;
import me.moirai.storyengine.core.port.inbound.world.CreateWorldLorebookEntryResult;
import me.moirai.storyengine.core.domain.world.WorldLorebookEntry;
import me.moirai.storyengine.core.domain.world.WorldService;
import io.micrometer.common.util.StringUtils;

@UseCaseHandler
public class CreateWorldLorebookEntryHandler
        extends AbstractUseCaseHandler<CreateWorldLorebookEntry, CreateWorldLorebookEntryResult> {

    private final WorldService domainService;

    public CreateWorldLorebookEntryHandler(WorldService domainService) {
        this.domainService = domainService;
    }

    @Override
    public void validate(CreateWorldLorebookEntry command) {

        if (StringUtils.isBlank(command.getWorldId())) {

            throw new IllegalArgumentException("World ID cannot be null");
        }

        if (StringUtils.isBlank(command.getName())) {

            throw new IllegalArgumentException("Name cannot be null");
        }

        if (StringUtils.isBlank(command.getDescription())) {

            throw new IllegalArgumentException("Description cannot be null");
        }
    }

    @Override
    public CreateWorldLorebookEntryResult execute(CreateWorldLorebookEntry command) {

        WorldLorebookEntry entry = domainService.createLorebookEntry(command);
        return CreateWorldLorebookEntryResult.build(entry.getId());
    }
}
