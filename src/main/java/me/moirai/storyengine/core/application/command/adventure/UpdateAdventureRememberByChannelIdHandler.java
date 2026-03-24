package me.moirai.storyengine.core.application.command.adventure;

import me.moirai.storyengine.common.annotation.UseCaseHandler;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.common.usecases.AbstractUseCaseHandler;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventureRememberByChannelId;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;

@UseCaseHandler
public class UpdateAdventureRememberByChannelIdHandler
        extends AbstractUseCaseHandler<UpdateAdventureRememberByChannelId, Void> {

    private static final String ADVENTURE_NOT_FOUND = "Adventure to be updated was not found";

    private AdventureRepository repository;

    public UpdateAdventureRememberByChannelIdHandler(AdventureRepository repository) {
        this.repository = repository;
    }

    @Override
    public Void execute(UpdateAdventureRememberByChannelId useCase) {

        repository.findByChannelId(useCase.getChannelId())
                .orElseThrow(() -> new AssetNotFoundException(ADVENTURE_NOT_FOUND));

        repository.updateRememberByChannelId(useCase.getRemember(), useCase.getChannelId());
        return null;
    }
}
