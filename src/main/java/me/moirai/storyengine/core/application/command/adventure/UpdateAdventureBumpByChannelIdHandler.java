package me.moirai.storyengine.core.application.command.adventure;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventureBumpByChannelId;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;

@UseCaseHandler
public class UpdateAdventureBumpByChannelIdHandler
        extends AbstractUseCaseHandler<UpdateAdventureBumpByChannelId, Void> {

    private static final String ADVENTURE_NOT_FOUND = "Adventure to be updated was not found";

    private AdventureRepository repository;

    public UpdateAdventureBumpByChannelIdHandler(AdventureRepository repository) {
        this.repository = repository;
    }

    @Override
    public Void execute(UpdateAdventureBumpByChannelId useCase) {

        repository.findByChannelId(useCase.getChannelId())
                .orElseThrow(() -> new AssetNotFoundException(ADVENTURE_NOT_FOUND));

        repository.updateBumpByChannelId(useCase.getBump(), useCase.getBumpFrequency(), useCase.getChannelId());
        return null;
    }
}
