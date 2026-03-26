package me.moirai.storyengine.core.application.command.adventure;

import me.moirai.storyengine.common.annotation.CommandHandler;
import me.moirai.storyengine.common.cqs.command.AbstractCommandHandler;
import me.moirai.storyengine.common.exception.AssetNotFoundException;
import me.moirai.storyengine.core.port.inbound.adventure.UpdateAdventureSceneById;
import me.moirai.storyengine.core.port.outbound.adventure.AdventureRepository;

@CommandHandler
public class UpdateAdventureSceneByIdHandler
        extends AbstractCommandHandler<UpdateAdventureSceneById, Void> {

    private static final String ADVENTURE_NOT_FOUND = "Adventure to be updated was not found";

    private AdventureRepository repository;

    public UpdateAdventureSceneByIdHandler(AdventureRepository repository) {
        this.repository = repository;
    }

    @Override
    public Void execute(UpdateAdventureSceneById useCase) {

        repository.findByPublicId(useCase.adventureId())
                .orElseThrow(() -> new AssetNotFoundException(ADVENTURE_NOT_FOUND));

        repository.updateSceneByPublicId(useCase.scene(), useCase.adventureId());

        return null;
    }
}
