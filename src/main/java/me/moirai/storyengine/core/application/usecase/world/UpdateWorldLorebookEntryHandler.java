package me.moirai.storyengine.core.application.usecase.world;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.application.usecase.world.request.UpdateWorldLorebookEntry;
import me.moirai.storyengine.core.application.usecase.world.result.UpdateWorldLorebookEntryResult;
import me.moirai.storyengine.core.domain.world.WorldLorebookEntry;
import me.moirai.storyengine.core.domain.world.WorldService;

@UseCaseHandler
public class UpdateWorldLorebookEntryHandler
        extends AbstractUseCaseHandler<UpdateWorldLorebookEntry, UpdateWorldLorebookEntryResult> {

    private final WorldService service;

    public UpdateWorldLorebookEntryHandler(WorldService service) {
        this.service = service;
    }

    @Override
    public UpdateWorldLorebookEntryResult execute(UpdateWorldLorebookEntry command) {

        return mapResult(service.updateLorebookEntry(command));
    }

    private UpdateWorldLorebookEntryResult mapResult(WorldLorebookEntry savedEntry) {

        return UpdateWorldLorebookEntryResult.build(savedEntry.getLastUpdateDate());
    }
}
