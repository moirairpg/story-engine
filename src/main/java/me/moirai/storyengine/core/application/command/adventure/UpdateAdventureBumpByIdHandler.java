package me.moirai.storyengine.core.application.command.adventure;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventureBumpById;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;

@CommandHandler
public class UpdateAdventureBumpByIdHandler
        extends AbstractCommandHandler<UpdateAdventureBumpById, Void> {

    private static final String ADVENTURE_NOT_FOUND = "Adventure to be updated was not found";

    private AdventureRepository repository;

    public UpdateAdventureBumpByIdHandler(AdventureRepository repository) {
        this.repository = repository;
    }

    @Override
    public Void execute(UpdateAdventureBumpById useCase) {

        repository.findByPublicId(useCase.adventureId())
                .orElseThrow(() -> new AssetNotFoundException(ADVENTURE_NOT_FOUND));

        repository.updateBumpByPublicId(useCase.bump(), useCase.bumpFrequency(), useCase.adventureId());

        return null;
    }
}
